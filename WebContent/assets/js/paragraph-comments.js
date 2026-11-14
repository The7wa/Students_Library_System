/* 墨卷书阁 · 段落公开评论弹窗
 * 只展示公开评论，不读取/展示任何个人批注内容。
 */
(function () {
    'use strict';

    var modal = document.getElementById('pCommentModal');
    if (!modal) {
        return;
    }

    var titleEl = document.getElementById('pCommentTitle');
    var topicEl = document.getElementById('pCommentTopic');
    var listEl = document.getElementById('pCommentList');
    var form = document.getElementById('pCommentForm');
    var paragraphInput = document.getElementById('pCommentParagraph');
    var quoteInput = document.getElementById('pCommentQuote');
    var parentInput = document.getElementById('pCommentParent');
    var actionInput = document.getElementById('pCommentAction');
    var contentInput = document.getElementById('pCommentContent');
    var replyHint = document.getElementById('pCommentReplyHint');
    var replyName = document.getElementById('pCommentReplyName');
    var cancelReplyBtn = document.getElementById('pCommentCancelReply');

    var comments = window.PARAGRAPH_COMMENTS || [];
    var currentParagraph = 0;
    var currentQuote = '';

    function formatTime(value) {
        if (!value) {
            return '';
        }
        return String(value).replace('T', ' ').substring(0, 16);
    }

    function resetReply() {
        parentInput.value = '';
        actionInput.value = 'add';
        if (replyHint) {
            replyHint.hidden = true;
        }
        if (replyName) {
            replyName.textContent = '';
        }
    }

    function setReply(comment) {
        parentInput.value = comment.parentId == null ? comment.id : comment.parentId;
        actionInput.value = 'reply';
        if (replyHint) {
            replyHint.hidden = false;
        }
        if (replyName) {
            replyName.textContent = comment.user || '';
        }
        if (contentInput) {
            contentInput.focus();
        }
    }

    function renderComment(comment, isReply) {
        var item = document.createElement('div');
        item.className = 'p-comment-item' + (isReply ? ' reply' : '');

        var avatar = document.createElement('span');
        avatar.className = 'comment-avatar';
        avatar.textContent = comment.avatar || (comment.user ? comment.user.substring(0, 1) : '读');

        var body = document.createElement('div');
        body.className = 'p-comment-item-body';

        var head = document.createElement('div');
        head.className = 'p-comment-item-head';
        var name = document.createElement('strong');
        name.textContent = comment.user || '读者';
        if (comment.mine) {
            name.textContent += '（我）';
        }
        var time = document.createElement('span');
        time.textContent = formatTime(comment.createdAt);
        head.appendChild(name);
        head.appendChild(time);
        if (isReply) {
            var badge = document.createElement('span');
            badge.className = 'p-comment-reply-badge';
            badge.textContent = '回复';
            head.appendChild(badge);
        }

        var text = document.createElement('p');
        text.textContent = comment.content || '';

        var tools = document.createElement('div');
        tools.className = 'p-comment-item-tools';
        var replyBtn = document.createElement('button');
        replyBtn.type = 'button';
        replyBtn.textContent = '回复';
        replyBtn.addEventListener('click', function () {
            setReply(comment);
        });
        tools.appendChild(replyBtn);

        body.appendChild(head);
        body.appendChild(text);
        body.appendChild(tools);
        item.appendChild(avatar);
        item.appendChild(body);
        return item;
    }

    function renderList() {
        if (!listEl) {
            return;
        }
        listEl.textContent = '';
        var roots = comments.filter(function (c) {
            return Number(c.p) === currentParagraph && (c.parentId == null || c.parentId === '');
        });
        if (!roots.length) {
            var empty = document.createElement('p');
            empty.className = 'p-comment-empty';
            empty.textContent = '还没有评论，来发表第一条公开评论吧。';
            listEl.appendChild(empty);
            return;
        }
        roots.forEach(function (root) {
            listEl.appendChild(renderComment(root, false));
            comments.filter(function (c) {
                return Number(c.p) === currentParagraph && Number(c.parentId) === Number(root.id);
            }).forEach(function (child) {
                listEl.appendChild(renderComment(child, true));
            });
        });
    }

    function openModal(paragraph, quote) {
        currentParagraph = Math.max(0, Number(paragraph) || 0);
        currentQuote = (quote || '').trim();
        if (currentQuote.length > 500) {
            currentQuote = currentQuote.substring(0, 500);
        }
        if (titleEl) {
            titleEl.textContent = '第 ' + (currentParagraph + 1) + ' 段评论';
        }
        if (topicEl) {
            topicEl.textContent = currentQuote
                ? '“' + currentQuote + '”'
                : '针对本段文字的公开讨论';
        }
        if (paragraphInput) {
            paragraphInput.value = String(currentParagraph);
        }
        if (quoteInput) {
            quoteInput.value = currentQuote;
        }
        resetReply();
        renderList();
        modal.hidden = false;
        document.body.classList.add('comment-modal-open');
        if (contentInput) {
            contentInput.value = '';
            contentInput.focus();
        }
    }

    function closeModal() {
        modal.hidden = true;
        document.body.classList.remove('comment-modal-open');
    }

    /* 段落评论数徽标 */
    document.querySelectorAll('.p-comment-badge').forEach(function (button) {
        button.addEventListener('click', function () {
            var paragraph = Number(button.getAttribute('data-comment-p')) || 0;
            var paragraphEl = button.closest('.rd-p-block')
                ? button.closest('.rd-p-block').querySelector('.rd-p')
                : null;
            openModal(paragraph, paragraphEl ? paragraphEl.textContent.trim() : '');
        });
    });

    /* 划选后的批注弹窗 → 公开评论 */
    var annoCommentBtn = document.getElementById('annoCommentBtn');
    if (annoCommentBtn) {
        annoCommentBtn.addEventListener('click', function () {
            var paragraphEl = document.getElementById('annoParagraph');
            var quoteEl = document.getElementById('annoQuote');
            var popup = document.getElementById('annoPopup');
            if (popup) {
                popup.hidden = true;
            }
            openModal(paragraphEl ? paragraphEl.value : 0, quoteEl ? quoteEl.value : '');
        });
    }

    modal.querySelectorAll('[data-comment-close]').forEach(function (el) {
        el.addEventListener('click', closeModal);
    });

    if (cancelReplyBtn) {
        cancelReplyBtn.addEventListener('click', resetReply);
    }

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeModal();
        }
    });

    /* 提交后服务端重定向到 #comments-p-N，自动重新打开 */
    function openFromHash() {
        var match = (location.hash || '').match(/^#comments-p-(\d+)/);
        if (!match) {
            return;
        }
        var p = Number(match[1]) || 0;
        var paragraphEl = document.querySelector('.rd-p[data-p="' + p + '"]');
        openModal(p, paragraphEl ? paragraphEl.textContent.trim() : '');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', openFromHash);
    } else {
        openFromHash();
    }
})();
