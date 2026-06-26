const DB_NAME = "web-vocab-learning";
const DB_VERSION = 1;
const STATE_KEY = "app-state";
const APP_VERSION_CODE = 1;
const APP_VERSION_NAME = "0.1.0";
const PUBLIC_RELEASE_REPO = "17521161324-byte/english-learning-app-release";
const UPDATE_FEED_URLS = [
  `https://github.com/${PUBLIC_RELEASE_REPO}/releases/latest/download/latest.json`,
  `https://raw.githubusercontent.com/${PUBLIC_RELEASE_REPO}/main/latest.json`,
];
const RELEASES_URL = `https://github.com/${PUBLIC_RELEASE_REPO}/releases/latest`;

const todayKey = new Date().toISOString().slice(0, 10);
const todayText = new Intl.DateTimeFormat("zh-CN", {
  month: "long",
  day: "numeric",
}).format(new Date());

const seedWords = [
  {
    word: "acquisition",
    phonetic: "/ˌækwɪˈzɪʃn/",
    meaning: "获得；收购",
    sentence: "The company announced its latest acquisition after months of negotiations.",
    source: "TechCrunch",
    url: "https://techcrunch.com",
    tag: "商业",
    status: "due",
    note: "类似 acquire，名词形式，常见于商业新闻。",
  },
  {
    word: "constraint",
    phonetic: "/kənˈstreɪnt/",
    meaning: "限制；约束",
    sentence: "This design has several technical constraints.",
    source: "MDN Docs",
    url: "https://developer.mozilla.org",
    tag: "技术",
    status: "new",
    note: "常用于技术文档和产品约束。",
  },
  {
    word: "practical",
    phonetic: "/ˈpræktɪkəl/",
    meaning: "实用的；实际的",
    sentence: "A practical approach often works better than a perfect plan.",
    source: "Paul Graham essay",
    url: "https://paulgraham.com",
    tag: "写作",
    status: "unsure",
    note: "常见于建议类文章。",
  },
  {
    word: "nuance",
    phonetic: "/ˈnuːɑːns/",
    meaning: "细微差别",
    sentence: "The article misses the nuance of how people actually learn.",
    source: "The Atlantic",
    url: "https://www.theatlantic.com",
    tag: "阅读",
    status: "mastered",
    note: "适合口语表达观点时使用。",
  },
  {
    word: "assumption",
    phonetic: "/əˈsʌmpʃn/",
    meaning: "假设；设想",
    sentence: "The argument depends on an assumption that may not be true.",
    source: "Hacker News",
    url: "https://news.ycombinator.com",
    tag: "逻辑",
    status: "due",
    note: "写作和讨论里都很常见。",
  },
];

const defaultSettings = {
  popup: true,
  sentence: true,
  source: true,
  offline: true,
  reminder: true,
  dailyWordGoal: 20,
  speakingGoal: 1,
};

const state = {
  ready: false,
  route: "today",
  tab: "全部",
  speakingMode: "场景",
  query: "",
  selectedWordId: "",
  formMode: "",
  words: [],
  planByDate: {},
  checkins: {},
  speakingSessions: [],
  settings: { ...defaultSettings },
  recording: false,
  seconds: 30,
  timer: null,
  mediaRecorder: null,
  recordingChunks: [],
  deferredPrompt: null,
  update: {
    checking: false,
    info: null,
    error: "",
    lastCheckedAt: "",
  },
};

let db;

const screen = document.querySelector("#screen");
const navButtons = document.querySelectorAll(".nav-item");
const resetButton = document.querySelector("#demo-reset");
const installButton = document.querySelector("#install-app");
const exportButton = document.querySelector("#export-data");
const importInput = document.querySelector("#import-data");

function uid(prefix = "id") {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function createDefaultState() {
  const now = new Date().toISOString();
  const words = seedWords.map((word, index) => ({
    ...word,
    id: uid("word"),
    createdAt: now,
    reviewCount: index % 2,
    lastReviewedAt: "",
    nextReviewAt: index === 3 ? "" : todayKey,
  }));

  return {
    words,
    selectedWordId: words[0].id,
    planByDate: { [todayKey]: [false, false, false] },
    checkins: {},
    speakingSessions: [],
    settings: { ...defaultSettings },
  };
}

function openDb() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const database = request.result;
      if (!database.objectStoreNames.contains("kv")) database.createObjectStore("kv");
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

function dbGet(key) {
  return new Promise((resolve, reject) => {
    const tx = db.transaction("kv", "readonly");
    const request = tx.objectStore("kv").get(key);
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

function dbPut(key, value) {
  return new Promise((resolve, reject) => {
    const tx = db.transaction("kv", "readwrite");
    tx.objectStore("kv").put(value, key);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

function serializableState() {
  return {
    words: state.words,
    selectedWordId: state.selectedWordId,
    planByDate: state.planByDate,
    checkins: state.checkins,
    speakingSessions: state.speakingSessions,
    settings: state.settings,
  };
}

async function saveState() {
  if (!state.ready) return;
  await dbPut(STATE_KEY, serializableState());
}

async function init() {
  db = await openDb();
  const stored = await dbGet(STATE_KEY);
  const defaults = createDefaultState();
  const data = stored || defaults;
  state.words = data.words?.length ? data.words : defaults.words;
  state.selectedWordId = data.selectedWordId || state.words[0]?.id || "";
  state.planByDate = data.planByDate || defaults.planByDate;
  state.checkins = data.checkins || {};
  state.speakingSessions = data.speakingSessions || [];
  state.settings = { ...defaultSettings, ...(data.settings || {}) };
  state.ready = true;
  await saveState();
  render();
  checkForUpdate(false);
}

function selectedWord() {
  return state.words.find((word) => word.id === state.selectedWordId) || state.words[0];
}

function todayPlan() {
  if (!state.planByDate[todayKey]) state.planByDate[todayKey] = [false, false, false];
  return state.planByDate[todayKey];
}

function dueWords() {
  return state.words.filter((word) => word.status === "due" || word.status === "unsure" || word.nextReviewAt === todayKey);
}

function statusLabel(status) {
  const map = {
    due: ["今日复习", "green"],
    new: ["新收藏", "blue"],
    unsure: ["有点模糊", "amber"],
    mastered: ["已掌握", "green"],
  };
  return map[status] || ["新收藏", "blue"];
}

function setRoute(route) {
  state.route = route;
  navButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.route === route);
  });
  render();
}

function showToast(message) {
  let toast = document.querySelector(".toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.className = "toast";
    document.body.append(toast);
  }
  toast.textContent = message;
  toast.classList.add("show");
  clearTimeout(showToast.timeout);
  showToast.timeout = setTimeout(() => toast.classList.remove("show"), 1900);
}

function render() {
  if (!state.ready) {
    screen.innerHTML = `<div class="empty-state">正在读取本地学习数据...</div>`;
    return;
  }
  const views = {
    today: renderToday,
    vocabulary: renderVocabulary,
    review: renderReview,
    wordDetail: renderWordDetail,
    wordForm: renderWordForm,
    speaking: renderSpeaking,
    checkin: renderCheckin,
    profile: renderProfile,
    settings: renderSettings,
  };
  screen.innerHTML = views[state.route]();
  bindViewEvents();
}

function pageHead(title, subtitle = "", action = "") {
  return `
    <header class="page-head">
      <div class="top-row">
        <div>
          <h2 class="page-title">${title}</h2>
          ${subtitle ? `<p class="subtle">${subtitle}</p>` : ""}
        </div>
        ${action}
      </div>
    </header>
  `;
}

function wordRows(words = state.words) {
  if (!words.length) return `<div class="empty-state">没有匹配的生词</div>`;
  return words
    .map((item) => {
      const [label, color] = statusLabel(item.status);
      return `
        <button class="list-row word-row" data-word-id="${item.id}">
          <span>
            <span class="row-title">${item.word}</span>
            <span class="row-copy">${item.sentence}</span>
            <span class="row-copy">${item.source} · ${item.tag}</span>
          </span>
          <span class="status-pill ${color}">${label}</span>
        </button>
      `;
    })
    .join("");
}

function renderToday() {
  const plan = todayPlan();
  const completed = plan.filter(Boolean).length;
  const planItems = ["复习网页生词", "完成口语练习", "回看模糊词"];
  const subtitle = `${todayText} · 连续 ${streakCount()} 天`;
  return `
    ${pageHead("今日", subtitle)}
    <section class="stats surface">
      <div class="stat"><strong>${dueWords().length} / ${state.settings.dailyWordGoal}</strong><span>待复习</span></div>
      <div class="stat"><strong>${state.settings.speakingGoal} 个</strong><span>口语任务</span></div>
      <div class="stat"><strong>${completed} / 3</strong><span>今日计划</span></div>
    </section>

    <section class="section">
      <h3 class="section-title">今日计划</h3>
      <div class="list">
        ${planItems
          .map(
            (item, index) => `
              <button class="list-row check-row ${plan[index] ? "done" : ""}" data-plan="${index}">
                <span class="checkbox"></span>
                <span>
                  <span class="row-title">${item}</span>
                  <span class="row-copy">${index === 0 ? "来自网页生词库" : index === 1 ? "使用今天收藏的词" : "优先巩固不稳定记忆"}</span>
                </span>
                <span class="status-pill ${plan[index] ? "green" : ""}">${plan[index] ? "完成" : "待做"}</span>
              </button>
            `,
          )
          .join("")}
      </div>
    </section>

    <section class="section">
      <div class="split-row">
        <h3 class="section-title">最近收藏</h3>
        <button class="ghost-button compact" data-go="vocabulary">查看全部</button>
      </div>
      <div class="list">${wordRows([...state.words].slice(0, 3))}</div>
    </section>

    <section class="section">
      <div class="button-row">
        <button class="primary-button" data-go="review">开始复习</button>
        <button class="secondary-button" data-go="speaking">口语练习</button>
        <button class="secondary-button" data-new-word>添加生词</button>
      </div>
    </section>
  `;
}

function filteredWords() {
  const query = state.query.trim().toLowerCase();
  return state.words.filter((word) => {
    const tabMatched =
      state.tab === "全部" ||
      (state.tab === "待复习" && (word.status === "due" || word.status === "unsure")) ||
      (state.tab === "新收藏" && word.status === "new") ||
      (state.tab === "已掌握" && word.status === "mastered") ||
      (state.tab === "短语" && word.word.includes(" "));
    const queryMatched =
      !query ||
      [word.word, word.meaning, word.source, word.tag, word.note, word.sentence].some((value) =>
        String(value || "").toLowerCase().includes(query),
      );
    return tabMatched && queryMatched;
  });
}

function renderVocabulary() {
  const tabs = ["全部", "待复习", "新收藏", "已掌握", "短语"];
  return `
    ${pageHead("网页生词库", `${state.words.length} 个收藏`, `<button class="primary-button compact" data-new-word>新增</button>`)}
    <input class="search" data-search placeholder="搜索单词、来源、笔记" value="${state.query}" />
    <section class="section">
      <div class="tabs">
        ${tabs.map((tab) => `<button class="tab ${state.tab === tab ? "active" : ""}" data-tab="${tab}">${tab}</button>`).join("")}
      </div>
    </section>
    <section class="section filters">
      <select class="select"><option>来源</option><option>MDN Docs</option><option>TechCrunch</option></select>
      <select class="select"><option>标签</option><option>技术</option><option>商业</option></select>
      <select class="select"><option>熟悉度</option><option>新收藏</option><option>已掌握</option></select>
    </section>
    <section class="section">
      <div class="list">${wordRows(filteredWords())}</div>
    </section>
    <section class="section button-row">
      <button class="primary-button" data-go="review">开始复习</button>
      <button class="secondary-button" data-new-word>手动添加</button>
      <button class="secondary-button" data-go="settings">同步设置</button>
    </section>
  `;
}

function renderReview() {
  const item = selectedWord();
  if (!item) return `<div class="empty-state">先添加一个生词，再开始复习。</div>`;
  const due = dueWords();
  const progress = Math.max(1, due.findIndex((word) => word.id === item.id) + 1);
  return `
    ${pageHead("复习", `今日待复习 ${due.length} 个 · ${progress || 1} / ${Math.max(due.length, 1)}`)}
    <section class="review-card surface">
      <div class="split-row">
        <div>
          <h2 class="word">${item.word}</h2>
          <span class="phonetic">${item.phonetic || "未填写音标"}</span>
        </div>
        <button class="secondary-button compact" data-speak>发音</button>
      </div>
      <p class="definition">${item.meaning || "未填写释义"}</p>
    </section>

    <section class="section">
      <h3 class="section-title">原文语境</h3>
      <div class="soft-surface review-card">
        <blockquote class="quote">${item.sentence || "未保存原句"}</blockquote>
        <p><a class="source-link" href="${item.url || "#"}" target="_blank" rel="noreferrer">${item.source || "未填写来源"} · 打开原文</a></p>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">我的笔记</h3>
      <textarea class="note-box" data-note>${item.note || ""}</textarea>
    </section>

    <section class="section">
      <h3 class="section-title">口语连接</h3>
      <button class="list-row" data-go="speaking">
        <span>
          <span class="row-title">试着用这个词做一段 30 秒表达。</span>
          <span class="row-copy">自动带入今日口语练习</span>
        </span>
        <span class="status-pill blue">去练习</span>
      </button>
    </section>

    <div class="review-actions">
      <button class="danger-button" data-rate="new">不认识</button>
      <button class="secondary-button" data-rate="unsure">有点模糊</button>
      <button class="primary-button" data-rate="mastered">已掌握</button>
    </div>
  `;
}

function renderWordDetail() {
  const item = selectedWord();
  if (!item) return `<div class="empty-state">没有选中的生词</div>`;
  return `
    <header class="page-head">
      <button class="ghost-button compact" data-go="vocabulary">返回</button>
      <h2 class="page-title">${item.word}</h2>
      <p class="subtle">${item.phonetic || "未填写音标"}</p>
    </header>

    <section class="review-card surface">
      <div class="split-row">
        <p class="definition">${item.meaning || "未填写释义"}</p>
        <button class="secondary-button compact" data-speak>发音</button>
      </div>
      <div class="section inline-row">
        <button class="tab ${item.status === "new" ? "active" : ""}" data-rate="new">新收藏</button>
        <button class="tab ${item.status === "unsure" || item.status === "due" ? "active" : ""}" data-rate="unsure">有点模糊</button>
        <button class="tab ${item.status === "mastered" ? "active" : ""}" data-rate="mastered">已掌握</button>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">原文语境</h3>
      <div class="list">
        <button class="list-row">
          <span><span class="row-title">${item.source || "自定义来源"}</span><span class="row-copy">${item.sentence || "未保存原句"}</span></span>
        </button>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">我的笔记</h3>
      <textarea class="note-box" data-note>${item.note || ""}</textarea>
    </section>

    <section class="section inline-row">
      <span class="tag">${item.tag || "未分类"}</span>
      <span class="tag">网页</span>
      <span class="tag">${item.reviewCount || 0} 次复习</span>
    </section>

    <section class="section button-row">
      <button class="primary-button" data-go="review">加入复习</button>
      <button class="secondary-button" data-edit-word>编辑</button>
      <button class="danger-button" data-delete-word>删除</button>
    </section>
  `;
}

function renderWordForm() {
  const item = selectedWord();
  const editing = Boolean(item && state.formMode === "edit");
  const data = editing ? item : {};
  return `
    <header class="page-head">
      <button class="ghost-button compact" data-go="vocabulary">返回</button>
      <h2 class="page-title">${editing ? "编辑生词" : "添加生词"}</h2>
      <p class="subtle">可以先填最关键的单词、释义和原句。</p>
    </header>
    <form class="form surface" data-word-form>
      ${formField("word", "单词 / 短语", data.word || "", "acquisition")}
      ${formField("meaning", "中文释义", data.meaning || "", "获得；收购")}
      ${formField("phonetic", "音标", data.phonetic || "", "/ˌækwɪˈzɪʃn/")}
      ${formArea("sentence", "原文语境", data.sentence || "", "The company announced its latest acquisition.")}
      ${formField("source", "来源", data.source || "", "TechCrunch")}
      ${formField("url", "原文链接", data.url || "", "https://...")}
      ${formField("tag", "标签", data.tag || "", "商业")}
      ${formArea("note", "我的笔记", data.note || "", "类似 acquire，名词形式。")}
      <button class="primary-button full" type="submit">${editing ? "保存修改" : "保存到生词库"}</button>
    </form>
  `;
}

function formField(name, label, value, placeholder) {
  return `
    <label class="field">
      <span>${label}</span>
      <input name="${name}" value="${escapeAttr(value)}" placeholder="${placeholder}" />
    </label>
  `;
}

function formArea(name, label, value, placeholder) {
  return `
    <label class="field">
      <span>${label}</span>
      <textarea name="${name}" placeholder="${placeholder}">${value}</textarea>
    </label>
  `;
}

function escapeAttr(value) {
  return String(value || "").replaceAll('"', "&quot;");
}

function renderSpeaking() {
  const words = state.words.slice(0, 3).map((item) => item.word);
  const sessions = state.speakingSessions.slice(0, 3);
  const mode = state.speakingMode;
  const content = speakingModeContent(mode, words);
  return `
    ${pageHead("口语练习", `已保存 ${state.speakingSessions.length} 次录音`)}
    <section class="tabs">
      ${["场景", "跟读", "自由表达"].map((item) => `<button class="tab ${mode === item ? "active" : ""}" data-speaking-mode="${item}">${item}</button>`).join("")}
    </section>

    <section class="section soft-surface review-card">
      <h3 class="section-title">${content.label}</h3>
      <span class="row-title">${content.title}</span>
      <p class="row-copy">${content.prompt}</p>
      <div class="inline-row">${words.map((word) => `<span class="tag">${word}</span>`).join("")}</div>
    </section>

    ${
      content.examples.length
        ? `<section class="section">
            <h3 class="section-title">示范句</h3>
            <div class="list">
              ${content.examples.map((example) => `<button class="list-row" data-speak-text="${escapeAttr(example)}"><span><span class="row-title">${example}</span><span class="row-copy">点击播放示范发音</span></span><span class="status-pill blue">播放</span></button>`).join("")}
            </div>
          </section>`
        : ""
    }

    <section class="section recorder surface ${state.recording ? "recording" : ""}">
      <button class="mic" data-record>${state.recording ? "■" : "●"}</button>
      <div class="timer">00:${String(state.seconds).padStart(2, "0")}</div>
      <div class="wave">
        ${[10, 18, 26, 14, 30, 20, 12, 28, 18, 24, 11, 21]
          .map((height, index) => `<span style="--h:${height}px;--i:${index}"></span>`)
          .join("")}
      </div>
      <button class="primary-button" data-record>${state.recording ? "停止录音" : "开始录音"}</button>
      <p class="row-copy">${content.hint}</p>
    </section>

    <section class="section">
      <h3 class="section-title">录音历史</h3>
      <div class="list">
        ${
          sessions.length
            ? sessions
                .map(
                  (session) => `
                    <button class="list-row">
                      <span><span class="row-title">${session.title}</span><span class="row-copy">${session.createdAt} · ${session.duration} 秒</span></span>
                      <span class="status-pill green">已保存</span>
                    </button>
                  `,
                )
                .join("")
            : `<div class="empty-state">还没有录音。点击开始录音试一次。</div>`
        }
      </div>
    </section>
  `;
}

function speakingModeContent(mode, words) {
  if (mode === "跟读") {
    return {
      label: "跟读练习",
      title: "先听示范，再录自己的版本",
      prompt: "跟读时重点模仿停顿、重音和句尾语调。",
      hint: "建议先播放示范句，再点击开始录音。",
      examples: [
        `The latest ${words[0] || "acquisition"} changed the company's strategy.`,
        `We need to understand the technical ${words[1] || "constraint"} first.`,
      ],
    };
  }
  if (mode === "自由表达") {
    return {
      label: "自由表达",
      title: "用今天的生词做 30 秒自由表达",
      prompt: "请围绕你今天读到的一篇英文文章，说出观点、原因和一个例子。",
      hint: "录音会保存到历史中，方便你之后回听。",
      examples: [],
    };
  }
  return {
    label: "今日主题",
    title: "用今天的网页生词做一次工作汇报",
    prompt: "请用 30 秒描述你今天读到的一篇英文文章。",
    hint: "说不完整也没关系，目标是把生词用出来。",
    examples: [],
  };
}

function renderCheckin() {
  const days = Array.from({ length: 30 }, (_, index) => index + 1);
  const completedDays = Object.values(state.checkins).filter(Boolean).length;
  return `
    ${pageHead("打卡", "本周学习")}
    <section class="stats surface">
      <div class="stat"><strong>连续 ${streakCount()} 天</strong><span>当前 streak</span></div>
      <div class="stat"><strong>${completedDays} 天</strong><span>已完成</span></div>
      <div class="stat"><strong>${state.words.length}</strong><span>总生词</span></div>
    </section>

    <section class="section calendar surface">
      <div class="week-grid">
        ${["一", "二", "三", "四", "五", "六", "日"].map((day) => `<span class="day-label">${day}</span>`).join("")}
      </div>
      <div class="calendar-grid section">
        ${days
          .map((day) => {
            const key = `${todayKey.slice(0, 8)}${String(day).padStart(2, "0")}`;
            return `<button class="day ${state.checkins[key] ? "done" : ""} ${day === Number(todayKey.slice(8)) ? "today" : ""}" data-day-key="${key}">${day}</button>`;
          })
          .join("")}
      </div>
    </section>

    <section class="section">
      <button class="primary-button full" data-checkin-today>${state.checkins[todayKey] ? "今日已打卡" : "完成今日打卡"}</button>
    </section>

    <section class="section">
      <h3 class="section-title">本周回顾</h3>
      <div class="list">
        <button class="list-row"><span><span class="row-title">复习 ${state.words.reduce((sum, word) => sum + (word.reviewCount || 0), 0)} 次</span><span class="row-copy">复习记录会自动保存在本机</span></span><span class="status-pill green">稳定</span></button>
        <button class="list-row"><span><span class="row-title">口语 ${state.speakingSessions.length} 次</span><span class="row-copy">录音元数据已保存</span></span><span class="status-pill blue">记录</span></button>
      </div>
    </section>
  `;
}

function streakCount() {
  let count = 0;
  const cursor = new Date(`${todayKey}T00:00:00`);
  for (let i = 0; i < 365; i += 1) {
    const key = cursor.toISOString().slice(0, 10);
    if (!state.checkins[key] && key !== todayKey) break;
    if (state.checkins[key] || key === todayKey) count += 1;
    cursor.setDate(cursor.getDate() - 1);
  }
  return Math.max(1, count);
}

function renderProfile() {
  return `
    ${pageHead("我的")}
    <section class="profile-card surface">
      <div class="inline-row">
        <span class="avatar">A</span>
        <span>
          <span class="row-title">Alex</span>
          <span class="row-copy">自用英语学习 · 本地优先保存</span>
        </span>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">每日设置</h3>
      <div class="list">
        <button class="list-row"><span><span class="row-title">每日生词目标</span><span class="row-copy">${state.settings.dailyWordGoal} 个</span></span></button>
        <button class="list-row"><span><span class="row-title">每日口语任务</span><span class="row-copy">${state.settings.speakingGoal} 个</span></span></button>
        <button class="list-row"><span><span class="row-title">复习提醒</span><span class="row-copy">21:30</span></span><span class="status-pill green">${state.settings.reminder ? "开启" : "关闭"}</span></button>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">数据</h3>
      <div class="list">
        <button class="list-row" data-go="vocabulary"><span><span class="row-title">我的单词本</span><span class="row-copy">${state.words.length} 个网页生词</span></span></button>
        <button class="list-row" data-export><span><span class="row-title">导出学习数据</span><span class="row-copy">生成 JSON 备份文件</span></span></button>
        <button class="list-row" data-import-trigger><span><span class="row-title">导入学习数据</span><span class="row-copy">恢复之前的 JSON 备份</span></span></button>
        <button class="list-row" data-go="settings"><span><span class="row-title">Chrome 插件同步</span><span class="row-copy">后续接入 · 当前本地保存</span></span><span class="status-pill amber">待开发</span></button>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">应用</h3>
      <div class="list">
        <button class="list-row" data-go="settings"><span><span class="row-title">版本与更新</span><span class="row-copy">当前版本 ${APP_VERSION_NAME} (${APP_VERSION_CODE})</span></span><span class="status-pill ${state.update.info?.hasUpdate ? "amber" : "green"}">${state.update.info?.hasUpdate ? "有更新" : "正常"}</span></button>
      </div>
    </section>
  `;
}

function renderSettings() {
  const update = state.update;
  const updateCopy = update.checking
    ? "正在检查 GitHub Releases..."
    : update.error
      ? update.error
      : update.info?.hasUpdate
        ? `发现 ${update.info.versionName} (${update.info.versionCode})`
        : update.lastCheckedAt
          ? `已是最新 · ${update.lastCheckedAt}`
          : "检查 GitHub Releases 中的 latest.json";

  return `
    <header class="page-head">
      <button class="ghost-button compact" data-go="profile">返回</button>
      <h2 class="page-title">设置与同步</h2>
      <p class="subtle">本地数据已自动保存，插件同步后续接入。</p>
    </header>

    <section class="section">
      <h3 class="section-title">版本与更新</h3>
      <div class="list">
        <button class="list-row" data-check-update>
          <span><span class="row-title">检查更新</span><span class="row-copy">当前版本 ${APP_VERSION_NAME} (${APP_VERSION_CODE}) · ${updateCopy}</span></span>
          <span class="status-pill ${update.info?.hasUpdate ? "amber" : "blue"}">${update.checking ? "检查中" : "检查"}</span>
        </button>
        ${
          update.info?.hasUpdate
            ? `<button class="list-row" data-open-update><span><span class="row-title">下载新版 APK</span><span class="row-copy">${update.info.changelog || "打开 GitHub Release 下载并安装"}</span></span><span class="status-pill green">更新</span></button>`
            : `<button class="list-row" data-open-releases><span><span class="row-title">打开发布页</span><span class="row-copy">查看历史版本和 APK 下载</span></span><span class="status-pill">GitHub</span></button>`
        }
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">Chrome 插件</h3>
      <div class="list">
        ${toggleRow("划词后显示释义", "popup")}
        ${toggleRow("收藏时保存原句", "sentence")}
        ${toggleRow("自动添加来源网页", "source")}
        ${toggleRow("离线时暂存收藏", "offline")}
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">学习设置</h3>
      <div class="form surface">
        <label class="field"><span>每日生词上限</span><input type="number" min="1" max="200" data-setting-number="dailyWordGoal" value="${state.settings.dailyWordGoal}" /></label>
        <label class="field"><span>每日口语任务</span><input type="number" min="1" max="20" data-setting-number="speakingGoal" value="${state.settings.speakingGoal}" /></label>
      </div>
    </section>

    <section class="section">
      <h3 class="section-title">通知与数据</h3>
      <div class="list">
        ${toggleRow("每日提醒 21:30", "reminder")}
        <button class="list-row" data-export><span><span class="row-title">导出备份</span><span class="row-copy">下载完整学习数据</span></span></button>
        <button class="list-row" data-import-trigger><span><span class="row-title">导入备份</span><span class="row-copy">从 JSON 文件恢复</span></span></button>
      </div>
    </section>
  `;
}

function toggleRow(label, key) {
  return `
    <button class="list-row toggle-row" data-toggle="${key}">
      <span><span class="row-title">${label}</span></span>
      <span class="switch ${state.settings[key] ? "on" : ""}" aria-hidden="true"></span>
    </button>
  `;
}

function bindViewEvents() {
  screen.querySelectorAll("[data-go]").forEach((button) => button.addEventListener("click", () => setRoute(button.dataset.go)));

  screen.querySelectorAll("[data-plan]").forEach((button) => {
    button.addEventListener("click", async () => {
      const plan = todayPlan();
      const index = Number(button.dataset.plan);
      plan[index] = !plan[index];
      if (plan.every(Boolean)) state.checkins[todayKey] = true;
      await saveState();
      showToast(plan[index] ? "已完成一项今日计划" : "已恢复为待做");
      render();
    });
  });

  screen.querySelectorAll("[data-tab]").forEach((button) => {
    button.addEventListener("click", () => {
      state.tab = button.dataset.tab;
      render();
    });
  });

  screen.querySelectorAll(".word-row").forEach((button) => {
    button.addEventListener("click", () => {
      state.selectedWordId = button.dataset.wordId;
      state.formMode = "";
      setRoute("wordDetail");
    });
  });

  const search = screen.querySelector("[data-search]");
  if (search) {
    search.addEventListener("input", (event) => {
      state.query = event.target.value;
      render();
      const nextSearch = screen.querySelector("[data-search]");
      nextSearch?.focus();
      nextSearch?.setSelectionRange(state.query.length, state.query.length);
    });
  }

  screen.querySelectorAll("[data-rate]").forEach((button) => {
    button.addEventListener("click", async () => {
      const word = selectedWord();
      word.status = button.dataset.rate;
      word.reviewCount = (word.reviewCount || 0) + 1;
      word.lastReviewedAt = new Date().toISOString();
      word.nextReviewAt = nextReviewDate(word.status);
      await saveState();
      showToast(`已标记为：${statusLabel(word.status)[0]}`);
      selectNextDueWord();
      render();
    });
  });

  const note = screen.querySelector("[data-note]");
  if (note) {
    note.addEventListener("change", async (event) => {
      selectedWord().note = event.target.value;
      await saveState();
      showToast("笔记已保存");
    });
  }

  const form = screen.querySelector("[data-word-form]");
  if (form) form.addEventListener("submit", handleWordForm);

  screen.querySelector("[data-edit-word]")?.addEventListener("click", () => {
    state.formMode = "edit";
    setRoute("wordForm");
  });

  screen.querySelectorAll("[data-new-word]").forEach((button) => {
    button.addEventListener("click", () => {
      state.formMode = "";
      setRoute("wordForm");
    });
  });

  screen.querySelector("[data-delete-word]")?.addEventListener("click", async () => {
    const word = selectedWord();
    if (!word || !confirm(`删除 ${word.word}？`)) return;
    state.words = state.words.filter((item) => item.id !== word.id);
    state.selectedWordId = state.words[0]?.id || "";
    await saveState();
    showToast("已删除生词");
    setRoute("vocabulary");
  });

  screen.querySelectorAll("[data-record]").forEach((button) => button.addEventListener("click", toggleRecording));

  screen.querySelectorAll("[data-speaking-mode]").forEach((button) => {
    button.addEventListener("click", () => {
      state.speakingMode = button.dataset.speakingMode;
      render();
    });
  });

  screen.querySelectorAll("[data-toggle]").forEach((button) => {
    button.addEventListener("click", async () => {
      const key = button.dataset.toggle;
      state.settings[key] = !state.settings[key];
      await saveState();
      showToast(state.settings[key] ? "已开启" : "已关闭");
      render();
    });
  });

  screen.querySelectorAll("[data-setting-number]").forEach((input) => {
    input.addEventListener("change", async () => {
      state.settings[input.dataset.settingNumber] = Number(input.value);
      await saveState();
      showToast("设置已保存");
      render();
    });
  });

  screen.querySelector("[data-checkin-today]")?.addEventListener("click", async () => {
    state.checkins[todayKey] = true;
    await saveState();
    showToast("今日已打卡");
    render();
  });

  screen.querySelectorAll("[data-day-key]").forEach((button) => {
    button.addEventListener("click", async () => {
      const key = button.dataset.dayKey;
      state.checkins[key] = !state.checkins[key];
      await saveState();
      render();
    });
  });

  screen.querySelectorAll("[data-speak]").forEach((button) => button.addEventListener("click", () => speakWord(selectedWord()?.word)));
  screen.querySelectorAll("[data-speak-text]").forEach((button) => button.addEventListener("click", () => speakWord(button.dataset.speakText)));
  screen.querySelectorAll("[data-export]").forEach((button) => button.addEventListener("click", exportData));
  screen.querySelectorAll("[data-import-trigger]").forEach((button) => button.addEventListener("click", () => importInput.click()));
  screen.querySelector("[data-check-update]")?.addEventListener("click", () => checkForUpdate(true));
  screen.querySelector("[data-open-update]")?.addEventListener("click", () => openUpdateDownload());
  screen.querySelector("[data-open-releases]")?.addEventListener("click", () => openExternal(RELEASES_URL));
}

function nextReviewDate(status) {
  const date = new Date();
  const offsets = { new: 1, unsure: 1, due: 2, mastered: 7 };
  date.setDate(date.getDate() + (offsets[status] || 1));
  return date.toISOString().slice(0, 10);
}

function selectNextDueWord() {
  const current = selectedWord();
  const due = dueWords().filter((word) => word.id !== current?.id);
  state.selectedWordId = due[0]?.id || state.words[0]?.id || "";
}

async function handleWordForm(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const payload = Object.fromEntries(form.entries());
  if (!payload.word?.trim()) {
    showToast("请先填写单词");
    return;
  }
  if (state.formMode === "edit") {
    Object.assign(selectedWord(), payload);
  } else {
    const word = {
      id: uid("word"),
      createdAt: new Date().toISOString(),
      reviewCount: 0,
      lastReviewedAt: "",
      nextReviewAt: todayKey,
      status: "new",
      ...payload,
    };
    state.words.unshift(word);
    state.selectedWordId = word.id;
  }
  state.formMode = "";
  await saveState();
  showToast("生词已保存");
  setRoute("wordDetail");
}

async function toggleRecording() {
  if (state.recording) {
    state.mediaRecorder?.stop();
    return;
  }
  if (!navigator.mediaDevices?.getUserMedia || !window.MediaRecorder) {
    showToast("当前 WebView 不支持网页录音");
    return;
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    state.recordingChunks = [];
    state.mediaRecorder = new MediaRecorder(stream);
    state.mediaRecorder.ondataavailable = (event) => {
      if (event.data.size) state.recordingChunks.push(event.data);
    };
    state.mediaRecorder.onstop = async () => {
      stream.getTracks().forEach((track) => track.stop());
      clearInterval(state.timer);
      state.recording = false;
      const duration = 30 - state.seconds;
      state.seconds = 30;
      state.speakingSessions.unshift({
        id: uid("session"),
        title: `${state.speakingMode} · 网页生词口语练习`,
        createdAt: new Date().toLocaleString("zh-CN", { month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit" }),
        duration: Math.max(1, duration),
        targetWords: state.words.slice(0, 3).map((word) => word.word),
      });
      todayPlan()[1] = true;
      await saveState();
      showToast("录音已保存");
      render();
    };
    state.mediaRecorder.start();
    state.recording = true;
    state.seconds = 30;
    state.timer = setInterval(() => {
      state.seconds -= 1;
      if (state.seconds <= 0) state.mediaRecorder?.stop();
      render();
    }, 1000);
    showToast("开始录音");
    render();
  } catch (error) {
    console.error("Recording failed", error);
    showToast("录音启动失败，请检查系统麦克风权限");
  }
}

function speakWord(text) {
  if (!text || !window.speechSynthesis) {
    showToast("当前浏览器不支持发音");
    return;
  }
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "en-US";
  utterance.rate = 0.85;
  window.speechSynthesis.speak(utterance);
}

function exportData() {
  const blob = new Blob([JSON.stringify(serializableState(), null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `english-learning-backup-${todayKey}.json`;
  link.click();
  URL.revokeObjectURL(url);
  showToast("备份已导出");
}

async function checkForUpdate(showResult) {
  state.update.checking = true;
  state.update.error = "";
  render();
  try {
    const latest = await fetchLatestUpdateInfo();
    const versionCode = Number(latest.versionCode || 0);
    state.update.info = {
      ...latest,
      versionCode,
      hasUpdate: versionCode > APP_VERSION_CODE,
    };
    state.update.lastCheckedAt = new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });
    if (showResult) showToast(state.update.info.hasUpdate ? `发现新版本 ${latest.versionName}` : "当前已是最新版本");
  } catch (error) {
    state.update.error = "检查失败，请稍后再试";
    if (showResult) showToast(state.update.error);
  } finally {
    state.update.checking = false;
    render();
  }
}

function openUpdateDownload() {
  const info = state.update.info;
  if (!info?.apkUrl) {
    openExternal(RELEASES_URL);
    return;
  }
  openExternal(info.apkUrl);
}

function openExternal(url) {
  const browser = window.Capacitor?.Plugins?.Browser;
  if (browser?.open) {
    browser.open({ url });
    return;
  }
  window.open(url, "_blank", "noopener,noreferrer");
}

async function fetchLatestUpdateInfo() {
  let lastError;
  for (const url of UPDATE_FEED_URLS) {
    try {
      const response = await fetch(`${url}?t=${Date.now()}`, { cache: "no-store" });
      if (!response.ok) throw new Error("更新信息暂不可用");
      return await response.json();
    } catch (error) {
      lastError = error;
    }
  }
  throw lastError || new Error("更新信息暂不可用");
}

async function importData(file) {
  if (!file) return;
  try {
    const data = JSON.parse(await file.text());
    state.words = data.words || state.words;
    state.selectedWordId = data.selectedWordId || state.words[0]?.id || "";
    state.planByDate = data.planByDate || {};
    state.checkins = data.checkins || {};
    state.speakingSessions = data.speakingSessions || [];
    state.settings = { ...defaultSettings, ...(data.settings || {}) };
    await saveState();
    showToast("学习数据已导入");
    render();
  } catch {
    showToast("导入失败，请检查 JSON 文件");
  }
}

async function resetData() {
  if (!confirm("重置会清空当前本地学习数据，确定继续？")) return;
  const defaults = createDefaultState();
  Object.assign(state, defaults, {
    route: "today",
    tab: "全部",
    query: "",
    formMode: "",
    recording: false,
    seconds: 30,
  });
  await saveState();
  showToast("学习数据已重置");
  setRoute("today");
}

if ("serviceWorker" in navigator && location.protocol !== "file:") {
  navigator.serviceWorker.register("./service-worker.js").catch(() => {});
}

window.addEventListener("beforeinstallprompt", (event) => {
  event.preventDefault();
  state.deferredPrompt = event;
  installButton.hidden = false;
});

installButton?.addEventListener("click", async () => {
  if (!state.deferredPrompt) {
    showToast("请在浏览器菜单中选择安装应用");
    return;
  }
  state.deferredPrompt.prompt();
  await state.deferredPrompt.userChoice;
  state.deferredPrompt = null;
  installButton.hidden = true;
});

navButtons.forEach((button) => button.addEventListener("click", () => setRoute(button.dataset.route)));
resetButton.addEventListener("click", resetData);
exportButton.addEventListener("click", exportData);
importInput.addEventListener("change", (event) => importData(event.target.files[0]));

init().catch(() => {
  screen.innerHTML = `<div class="empty-state">本地数据库初始化失败，请换用 Chrome 或 Safari 再试。</div>`;
});
