/* 墨卷书阁 · 阅读页交互：字号、背景、目录、进度与键盘翻章 */
(function () {
    'use strict';

    var cfg = window.LIB_READER || {};
    var body = document.body;
    var PREF_KEY = 'mojuan-reader-prefs';
    var PROGRESS_PREFIX = 'mojuan-reader-progress-';
    var MIN_FONT = 15;
    var MAX_FONT = 30;
    var DEFAULT_FONT = 19;

    var THEMES = [
        { id: 'paper', icon: '🌙', label: '纸白' },
        { id: 'eye', icon: '🌿', label: '护眼' },
        { id: 'night', icon: '☀️', label: '夜间' }
    ];

    var prefs = { font: DEFAULT_FONT, theme: 'paper' };
    try {
        var storedPrefs = JSON.parse(localStorage.getItem(PREF_KEY) || 'null');
        if (storedPrefs) {
            if (typeof storedPrefs.font === 'number') {
                prefs.font = storedPrefs.font;
            }
            if (storedPrefs.theme) {
                prefs.theme = storedPrefs.theme;
            }
        }
    } catch (e) {
        /* 隐私模式下 localStorage 不可用，忽略即可 */
    }

    function themeInfo(id) {
        for (var i = 0; i < THEMES.length; i++) {
            if (THEMES[i].id === id) {
                return THEMES[i];
            }
        }
        return THEMES[0];
    }

    function applyPrefs() {
        prefs.font = Math.max(MIN_FONT, Math.min(MAX_FONT, prefs.font));
        body.setAttribute('data-theme', prefs.theme);
        document.documentElement.style.setProperty('--reader-font-size', prefs.font + 'px');

        var info = themeInfo(prefs.theme);
        var icon = document.querySelector('[data-theme-icon]');
        if (icon) {
            icon.textContent = info.icon;
        }
        var themeButton = document.querySelector('[data-reader-theme]');
        if (themeButton) {
            themeButton.title = '阅读背景：' + info.label + '（点击切换）';
        }
    }

    function savePrefs() {
        try {
            localStorage.setItem(PREF_KEY, JSON.stringify(prefs));
        } catch (e) {
            /* 忽略 */
        }
    }

    function currentRatio() {
        var max = document.documentElement.scrollHeight - window.innerHeight;
        if (max <= 0) {
            return 0;
        }
        return Math.min(1, Math.max(0, window.scrollY / max));
    }

    function progressKey() {
        return PROGRESS_PREFIX + cfg.bookId;
    }

    function saveProgress() {
        if (!cfg.bookId && cfg.bookId !== 0) {
            return;
        }
        try {
            localStorage.setItem(progressKey(), JSON.stringify({
                chapter: cfg.chapter,
                ratio: currentRatio(),
                ts: Date.now()
            }));
        } catch (e) {
            /* 忽略 */
        }
    }

    /* 字号 */
    document.querySelectorAll('[data-reader-font]').forEach(function (button) {
        button.addEventListener('click', function () {
            prefs.font += Number(button.getAttribute('data-reader-font')) || 0;
            applyPrefs();
            savePrefs();
        });
    });

    /* 背景 */
    var themeButton = document.querySelector('[data-reader-theme]');
    if (themeButton) {
        themeButton.addEventListener('click', function () {
            var index = 0;
            for (var i = 0; i < THEMES.length; i++) {
                if (THEMES[i].id === prefs.theme) {
                    index = i;
                }
            }
            prefs.theme = THEMES[(index + 1) % THEMES.length].id;
            applyPrefs();
            savePrefs();
        });
    }

    /* 目录抽屉 */
    var toc = document.getElementById('readerToc');
    var tocToggle = document.querySelector('[data-toc-toggle]');

    function setToc(open) {
        if (!toc) {
            return;
        }
        toc.classList.toggle('open', open);
        body.classList.toggle('toc-open', open);
    }

    if (tocToggle) {
        tocToggle.addEventListener('click', function () {
            setToc(!toc.classList.contains('open'));
        });
    }
    document.querySelectorAll('[data-toc-close]').forEach(function (button) {
        button.addEventListener('click', function () {
            setToc(false);
        });
    });

    /* 长篇小说的目录：打开时把当前章节滚到中间 */
    var activeTocItem = document.querySelector('.toc-list li.active');
    if (activeTocItem) {
        var tocList = activeTocItem.closest('.toc-list');
        if (tocList) {
            tocList.scrollTop = Math.max(0,
                activeTocItem.offsetTop - tocList.clientHeight / 2 + activeTocItem.offsetHeight / 2);
        }
    }

    /* 阅读进度条 + 本地进度 */
    var bar = document.querySelector('[data-read-progress]');
    var saveTimer = null;

    function refreshProgress() {
        if (bar) {
            bar.style.width = (currentRatio() * 100).toFixed(2) + '%';
        }
    }

    window.addEventListener('scroll', function () {
        refreshProgress();
        window.clearTimeout(saveTimer);
        saveTimer = window.setTimeout(saveProgress, 180);
    }, { passive: true });

    window.addEventListener('resize', refreshProgress);
    window.addEventListener('pagehide', saveProgress);

    refreshProgress();

    /* 恢复上次滚动位置（只恢复同一章） */
    try {
        var saved = JSON.parse(localStorage.getItem(progressKey()) || 'null');
        if (saved && saved.chapter === cfg.chapter && saved.ratio > 0.01) {
            window.addEventListener('load', function () {
                window.setTimeout(function () {
                    var max = document.documentElement.scrollHeight - window.innerHeight;
                    if (max > 0) {
                        window.scrollTo(0, saved.ratio * max);
                        refreshProgress();
                    }
                }, 80);
            });
        }
    } catch (e) {
        /* 忽略 */
    }

    /* 键盘翻章 */
    document.addEventListener('keydown', function (event) {
        if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) {
            return;
        }
        var tag = event.target && event.target.tagName;
        if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') {
            return;
        }
        if (event.key === 'ArrowLeft') {
            var prev = document.querySelector('[data-reader-prev]');
            if (prev) {
                event.preventDefault();
                prev.click();
            }
        } else if (event.key === 'ArrowRight') {
            var next = document.querySelector('[data-reader-next]');
            if (next) {
                event.preventDefault();
                next.click();
            }
        } else if (event.key === 'Escape') {
            setToc(false);
        }
    });

    applyPrefs();
})();
