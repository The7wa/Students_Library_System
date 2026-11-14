/* 阅读页：批注定位、划选笔记、颜色高亮（仅本人可见）。 */
(function () {
    'use strict';

    var readerText = document.getElementById('readerText');
    if (!readerText) {
        return;
    }

    var popup = document.getElementById('annoPopup');
    var form = document.getElementById('annoForm');
    var quoteInput = document.getElementById('annoQuote');
    var paragraphInput = document.getElementById('annoParagraph');
    var preview = document.getElementById('annoPreview');
    var noteInput = document.getElementById('annoNote');
    var cancelBtn = document.getElementById('annoCancel');

    /* ---------- 1. 已保存批注：按原文定位并高亮 ---------- */

    function locate(root, offset) {
        var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
        var remaining = offset;
        var node;
        while ((node = walker.nextNode())) {
            var len = node.nodeValue.length;
            if (remaining <= len) {
                return {node: node, offset: remaining};
            }
            remaining -= len;
        }
        return null;
    }

    function highlight(p, quote, color, id) {
        if (!quote) {
            return;
        }
        var index = p.textContent.indexOf(quote);
        if (index < 0) {
            return;
        }
        var start = locate(p, index);
        var end = locate(p, index + quote.length);
        if (!start || !end) {
            return;
        }
        var range = document.createRange();
        range.setStart(start.node, start.offset);
        range.setEnd(end.node, end.offset);
        var mark = document.createElement('mark');
        mark.className = 'anno-mark';
        mark.style.backgroundColor = color;
        mark.setAttribute('data-anno', id);
        try {
            range.surroundContents(mark);
        } catch (e) {
            /* 跨节点选区无法直接包裹时保持纯文本，仅保留侧栏定位 */
        }
    }

    var annotations = window.MY_ANNOTATIONS || [];
    var chapterIndex = window.CHAPTER_INDEX || 0;
    annotations.forEach(function (a) {
        if (a.chapter !== chapterIndex) {
            return;
        }
        var p = readerText.querySelector('.rd-p[data-p="' + a.p + '"]');
        if (p) {
            highlight(p, a.quote, a.color, a.id);
        }
    });

    /* ---------- 2. 点击批注定位 ---------- */

    function scrollToParagraph(index) {
        var target = readerText.querySelector('.rd-p[data-p="' + index + '"]');
        if (!target) {
            return;
        }
        target.scrollIntoView({behavior: 'smooth', block: 'center'});
        target.classList.add('p-flash');
        window.setTimeout(function () {
            target.classList.remove('p-flash');
        }, 1800);
        var marks = target.querySelectorAll('.anno-mark');
        for (var i = 0; i < marks.length; i++) {
            marks[i].classList.add('anno-flash');
        }
        window.setTimeout(function () {
            var flashing = target.querySelectorAll('.anno-flash');
            for (var j = 0; j < flashing.length; j++) {
                flashing[j].classList.remove('anno-flash');
            }
        }, 1800);
    }

    document.querySelectorAll('.note-jump, .note-item').forEach(function (el) {
        el.addEventListener('click', function (event) {
            if (event.target.closest('form')) {
                return;
            }
            var target = el.getAttribute('data-target') ||
                (el.closest('.note-item') && el.closest('.note-item').getAttribute('data-target'));
            if (target && target.indexOf('p-') === 0) {
                scrollToParagraph(target.substring(2));
            }
        });
    });

    /* ---------- 3. 划选文字 → 添加彩色笔记 ---------- */

    function hidePopup() {
        if (popup) {
            popup.hidden = true;
        }
    }

    function showPopup(rect, pIndex, text) {
        if (!popup || !form) {
            return;
        }
        paragraphInput.value = String(pIndex);
        quoteInput.value = text;
        preview.textContent = '“' + (text.length > 80 ? text.substring(0, 80) + '…' : text) + '”';
        noteInput.value = '';
        form.querySelector('input[name="color"][value="#f5c242"]').checked = true;
        popup.hidden = false;

        var width = 320;
        var left = Math.max(12, Math.min(rect.left, window.innerWidth - width - 12));
        var top = rect.bottom + 8;
        if (top + 300 > window.innerHeight) {
            top = Math.max(12, rect.top - 300);
        }
        popup.style.left = left + 'px';
        popup.style.top = top + 'px';
    }

    document.addEventListener('mouseup', function (event) {
        if (popup && popup.contains(event.target)) {
            return;
        }
        window.setTimeout(function () {
            var selection = window.getSelection();
            if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
                return;
            }
            var text = selection.toString().replace(/\s+/g, ' ').trim();
            if (!text) {
                return;
            }
            var range = selection.getRangeAt(0);
            var node = range.startContainer;
            var element = node.nodeType === 1 ? node : node.parentElement;
            var p = element ? element.closest('.rd-p') : null;
            if (!p || !readerText.contains(p) || !p.contains(range.endContainer)) {
                return;
            }
            if (text.length > 500) {
                text = text.substring(0, 500);
            }
            showPopup(range.getBoundingClientRect(), parseInt(p.getAttribute('data-p'), 10) || 0, text);
        }, 15);
    });

    if (cancelBtn) {
        cancelBtn.addEventListener('click', hidePopup);
    }

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            hidePopup();
        }
    });

    document.addEventListener('mousedown', function (event) {
        if (popup && !popup.hidden && !popup.contains(event.target)) {
            hidePopup();
        }
    });

    window.addEventListener('scroll', hidePopup, {passive: true});

    /* ---------- 4. 字号调整 ---------- */

    var FONT_KEY = 'mojuan-reader-font';
    var fontVal = document.getElementById('fontVal');
    var readerTextEl = document.getElementById('readerText');

    function applyFont(size) {
        size = Math.max(13, Math.min(28, size));
        if (readerTextEl) {
            readerTextEl.style.fontSize = size + 'px';
        }
        if (fontVal) {
            fontVal.textContent = size;
        }
        try { localStorage.setItem(FONT_KEY, size); } catch (e) {}
    }

    try {
        var savedFont = parseInt(localStorage.getItem(FONT_KEY), 10);
        if (savedFont) {
            applyFont(savedFont);
        }
    } catch (e) {}

    document.querySelectorAll('[data-font]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var current = parseInt((fontVal && fontVal.textContent) || '17', 10);
            applyFont(btn.getAttribute('data-font') === '+' ? current + 1 : current - 1);
        });
    });

    /* ---------- 5. 自动滚屏 + 速度 ---------- */

    var SPEED_KEY = 'mojuan-reader-speed';
    var autoBtn = document.getElementById('autoScrollBtn');
    var speedSel = document.getElementById('autoSpeed');
    var autoing = false;
    var autoRaf = null;
    var autoAcc = 0;
    var autoLast = 0;
    /* 每秒滚动像素：慢 / 中 / 快 / 飞快 */
    var SPEED_PX = {1: 18, 2: 45, 3: 95, 4: 190};

    function stopAuto() {
        autoing = false;
        if (autoRaf) {
            window.cancelAnimationFrame(autoRaf);
            autoRaf = null;
        }
        document.documentElement.style.scrollBehavior = '';
        if (autoBtn) {
            autoBtn.textContent = '自动滚动';
            autoBtn.classList.remove('running');
        }
    }

    function autoStep(now) {
        if (!autoing) {
            return;
        }
        if (!autoLast) {
            autoLast = now;
        }
        var dt = Math.min(120, now - autoLast) / 1000;
        autoLast = now;
        var speed = parseInt((speedSel && speedSel.value) || '2', 10);
        var pxPerSecond = SPEED_PX[speed] || 45;
        autoAcc += pxPerSecond * dt;
        var whole = Math.floor(autoAcc);
        if (whole > 0) {
            autoAcc -= whole;
            var before = window.scrollY;
            window.scrollBy(0, whole);
            if (window.scrollY === before) {
                stopAuto();
                return;
            }
        }
        var atBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2;
        if (atBottom) {
            stopAuto();
            return;
        }
        autoRaf = window.requestAnimationFrame(autoStep);
    }

    function startAuto() {
        if (autoing) {
            return;
        }
        autoing = true;
        autoAcc = 0;
        autoLast = 0;
        /* 关掉全局平滑滚动，否则连续 scrollBy 会被浏览器合并，各档速度看起来一样 */
        document.documentElement.style.scrollBehavior = 'auto';
        if (autoBtn) {
            autoBtn.textContent = '暂停滚动';
            autoBtn.classList.add('running');
        }
        autoRaf = window.requestAnimationFrame(autoStep);
    }

    if (autoBtn) {
        autoBtn.addEventListener('click', function () {
            if (autoing) {
                stopAuto();
            } else {
                startAuto();
            }
        });
    }

    if (speedSel) {
        try {
            var savedSpeed = localStorage.getItem(SPEED_KEY);
            if (savedSpeed) {
                speedSel.value = savedSpeed;
            }
        } catch (e) {}
        speedSel.addEventListener('change', function () {
            try { localStorage.setItem(SPEED_KEY, speedSel.value); } catch (e) {}
            if (autoing) {
                stopAuto();
                startAuto();
            }
        });
    }

    /* 用户手动滚动时暂停自动滚动 */
    ['wheel', 'touchstart'].forEach(function (evt) {
        window.addEventListener(evt, function () {
            if (autoing) {
                stopAuto();
            }
        }, {passive: true});
    });

    /* ---------- 6. 阅读进度：章节进度条 + 记忆位置 ---------- */

    var bookId = window.BOOK_ID;
    var chapterIndex = window.CHAPTER_INDEX || 0;
    var chapterCount = window.CHAPTER_COUNT || 1;
    var PROGRESS_KEY = 'mojuan-reader-progress-' + bookId;

    var bar = document.getElementById('chapterProgressBar');
    var barText = document.getElementById('chapterProgressText');

    function readProgress() {
        try {
            return JSON.parse(localStorage.getItem(PROGRESS_KEY) || 'null');
        } catch (e) {
            return null;
        }
    }

    /* 本章滚动比例 */
    function chapterRatio() {
        var max = document.body.scrollHeight - window.innerHeight;
        if (max <= 0) {
            return 1;
        }
        return Math.max(0, Math.min(1, window.scrollY / max));
    }

    /* 全书进度：已完成章节 + 当前章节内的比例 */
    function overallPercent(ratio) {
        if (chapterCount <= 1) {
            return Math.round(ratio * 100);
        }
        return Math.round(((chapterIndex + ratio) / chapterCount) * 100);
    }

    function saveProgress(ratio) {
        try {
            localStorage.setItem(PROGRESS_KEY, JSON.stringify({
                chapter: chapterIndex,
                ratio: ratio,
                percent: overallPercent(ratio),
                chapterCount: chapterCount,
                ts: Date.now()
            }));
            /* 兼容旧版本详情页的「继续阅读」 */
            localStorage.setItem('mojuan-reader-progress-' + bookId, JSON.stringify({
                chapter: chapterIndex,
                ratio: ratio,
                percent: overallPercent(ratio),
                chapterCount: chapterCount,
                ts: Date.now()
            }));
        } catch (e) {}
    }

    function updateBar() {
        var ratio = chapterRatio();
        if (bar) {
            bar.style.width = Math.round(ratio * 100) + '%';
        }
        if (barText) {
            barText.textContent = '本章 ' + Math.round(ratio * 100) + '%';
        }
        return ratio;
    }

    /* 打开时恢复上次位置 */
    (function restore() {
        var saved = readProgress();
        if (!saved || saved.chapter !== chapterIndex) {
            updateBar();
            return;
        }
        if (location.hash && location.hash.indexOf('#p-') === 0) {
            updateBar();
            return;
        }
        var targetRatio = typeof saved.ratio === 'number' ? saved.ratio : 0;
        if (targetRatio <= 0.002) {
            updateBar();
            return;
        }
        window.setTimeout(function () {
            var max = document.body.scrollHeight - window.innerHeight;
            window.scrollTo(0, Math.round(max * targetRatio));
            updateBar();
        }, 60);
    })();

    /* 滚动时更新进度条并节流保存 */
    var saveTimer = null;
    window.addEventListener('scroll', function () {
        var ratio = updateBar();
        if (saveTimer) {
            return;
        }
        saveTimer = window.setTimeout(function () {
            saveTimer = null;
            saveProgress(chapterRatio());
        }, 400);
    }, {passive: true});

    /* 离开页面前保存 */
    window.addEventListener('beforeunload', function () {
        saveProgress(chapterRatio());
    });
})();
