/* 墨卷书阁 · 轻量交互 */
(function () {
    'use strict';

    /* 删除等危险操作二次确认 */
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var message = form.getAttribute('data-confirm') || '确定执行这个操作吗？';
            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });

    /* 密码显示 / 隐藏 */
    document.querySelectorAll('[data-toggle-password]').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = document.getElementById(button.getAttribute('data-toggle-password'));
            if (!input) {
                return;
            }
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            button.textContent = show ? '隐藏' : '显示';
        });
    });

    /* 下拉排序自动提交 */
    document.querySelectorAll('select[data-autosubmit]').forEach(function (select) {
        select.addEventListener('change', function () {
            if (select.form) {
                select.form.submit();
            }
        });
    });

    /* 板块图标快捷选择 */
    document.querySelectorAll('[data-emoji]').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = document.getElementById('icon');
            if (input) {
                input.value = button.getAttribute('data-emoji');
                input.focus();
            }
        });
    });

    /* 提示条自动消失 */
    var flash = document.querySelector('.flash');
    if (flash) {
        var close = flash.querySelector('.flash-close');
        var hide = function () {
            flash.classList.add('hide');
            window.setTimeout(function () {
                var wrap = flash.closest('.flash-wrap');
                if (wrap) {
                    wrap.remove();
                }
            }, 320);
        };
        if (close) {
            close.addEventListener('click', hide);
        }
        window.setTimeout(hide, 4200);
    }

    /* 正文 TXT 文件选择 */
    var fileInput = document.querySelector('[data-file-input]');
    if (fileInput) {
        var fileName = document.querySelector('[data-file-name]');
        var fileDrop = fileInput.closest('.file-drop');

        var showFileName = function () {
            if (!fileName) {
                return;
            }
            var file = fileInput.files && fileInput.files[0];
            fileName.textContent = file ? file.name : '未选择文件';
            fileName.classList.toggle('has-file', !!file);
        };

        fileInput.addEventListener('change', showFileName);

        if (fileDrop) {
            fileDrop.addEventListener('dragover', function (event) {
                event.preventDefault();
                fileDrop.classList.add('dragover');
            });
            fileDrop.addEventListener('dragleave', function () {
                fileDrop.classList.remove('dragover');
            });
            fileDrop.addEventListener('drop', function (event) {
                event.preventDefault();
                fileDrop.classList.remove('dragover');
                if (event.dataTransfer && event.dataTransfer.files.length) {
                    fileInput.files = event.dataTransfer.files;
                    showFileName();
                }
            });
        }
    }

    /* 详情页：本地有阅读进度时，把「开始阅读」变成「继续阅读」 */
    document.querySelectorAll('[data-read-link]').forEach(function (link) {
        var bookId = link.getAttribute('data-book-id');
        if (!bookId) {
            return;
        }
        try {
            var saved = JSON.parse(localStorage.getItem('mojuan-reader-progress-' + bookId) || 'null');
            if (!saved || typeof saved.chapter !== 'number') {
                return;
            }
            var href = link.getAttribute('href') || '';
            if (href.indexOf('chapter=') === -1) {
                link.setAttribute('href', href + '&chapter=' + saved.chapter);
            }
            if (saved.chapter > 0 || saved.ratio > 0.02) {
                link.textContent = '继续阅读';
                link.title = '上次读到第 ' + (saved.chapter + 1) + ' 章';
            }
        } catch (e) {
            /* localStorage 不可用时保持默认按钮 */
        }
    });

    /* 封面阅读进度角标：读取本地阅读记录，显示「已读 XX%」 */
    document.querySelectorAll('[data-cover-book]').forEach(function (cover) {
        var bookId = cover.getAttribute('data-cover-book');
        if (!bookId) {
            return;
        }
        var progress = null;
        try {
            progress = JSON.parse(localStorage.getItem('mojuan-reader-progress-' + bookId) || 'null');
        } catch (e) {
            return;
        }
        if (!progress || typeof progress.percent !== 'number' || progress.percent <= 0) {
            return;
        }
        var percent = Math.min(100, Math.round(progress.percent));
        var badge = document.createElement('span');
        badge.className = 'cover-progress' + (percent >= 99 ? ' done' : '');
        badge.innerHTML = '<span class="cover-progress-track"><i style="width:' + percent + '%"></i></span>' +
            '<span class="cover-progress-text">' + (percent >= 99 ? '已读完' : '已读 ' + percent + '%') + '</span>';
        cover.appendChild(badge);
    });

    /* 全站背景主题切换：白 / 护眼绿 / 夜间黑（柔性过渡，全站记忆） */
    (function () {
        var THEMES = ['white', 'green', 'black'];
        var STORAGE_KEY = 'siteTheme';

        function apply(theme) {
            THEMES.forEach(function (t) {
                document.body.classList.remove('theme-' + t);
            });
            if (theme === 'green' || theme === 'black') {
                document.body.classList.add('theme-' + theme);
            }
            try { localStorage.setItem(STORAGE_KEY, theme); } catch (e) {}
            document.querySelectorAll('.site-theme .theme-dot').forEach(function (b) {
                b.classList.toggle('active', b.getAttribute('data-theme') === theme);
            });
        }

        /* 旧版阅读页局部主题迁移 */
        var saved = null;
        try {
            saved = localStorage.getItem(STORAGE_KEY) || localStorage.getItem('readerTheme');
            if (localStorage.getItem(STORAGE_KEY) === null && saved) {
                localStorage.setItem(STORAGE_KEY, saved);
            }
        } catch (e) {}
        if (saved === 'green' || saved === 'black') {
            document.body.classList.add('theme-' + saved);
        }

        /* 构建切换器 */
        var box = document.createElement('div');
        box.className = 'site-theme';
        box.setAttribute('role', 'group');
        box.setAttribute('aria-label', '切换全站背景主题');
        box.innerHTML =
            '<button type="button" class="theme-dot theme-white" data-theme="white" title="白色主题" aria-label="白色主题"></button>' +
            '<button type="button" class="theme-dot theme-green" data-theme="green" title="护眼绿主题" aria-label="护眼绿主题"></button>' +
            '<button type="button" class="theme-dot theme-black" data-theme="black" title="夜间黑主题" aria-label="夜间黑主题"></button>';

        var topbarInner = document.querySelector('.topbar-inner');
        if (topbarInner) {
            box.classList.add('in-topbar');
            var nav = topbarInner.querySelector('.topnav');
            if (nav) {
                topbarInner.insertBefore(box, nav);
            } else {
                topbarInner.appendChild(box);
            }
        } else {
            box.classList.add('floating');
            document.body.appendChild(box);
        }

        box.addEventListener('click', function (ev) {
            var btn = ev.target.closest('.theme-dot');
            if (btn) {
                apply(btn.getAttribute('data-theme'));
            }
        });

        /* 高亮当前主题 */
        document.querySelectorAll('.site-theme .theme-dot').forEach(function (b) {
            b.classList.toggle('active', b.getAttribute('data-theme') === saved);
        });
    })();
})();
