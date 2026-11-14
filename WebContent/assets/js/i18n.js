/* 墨卷书阁 · 界面语言（中文 / English）
 * 只翻译界面文案，图书、批注、评论等用户内容保持原文。
 */
(function () {
    'use strict';

    var lang = 'zh';
    try {
        lang = localStorage.getItem('siteLang') || 'zh';
    } catch (e) {
    }
    if (lang !== 'en') {
        return;
    }

    var DICT = {
        '首页': 'Home',
        '全部图书': 'All Books',
        '我的上传': 'My Uploads',
        '通知': 'Notifications',
        '管理': 'Admin',
        '管理员': 'Admin',
        '读者': 'Reader',
        '个人主页': 'Profile',
        '我的笔记': 'My Notes',
        '我的书架': 'My Bookshelf',
        '加入书架': 'Add to Shelf',
        '＋ 加入书架': '＋ Add to Shelf',
        '已在书架': 'In Shelf',
        '✓ 已在书架': '✓ In Shelf',
        '移出书架': 'Remove from Shelf',
        '移出': 'Remove',
        '继续阅读': 'Continue Reading',
        '继续阅读 →': 'Continue Reading →',
        '图书详情': 'Book Details',
        '书架还是空的': 'Your shelf is empty',
        '去逛书库 →': 'Browse the library →',
        '本书': 'books',
        '暂无正文': 'No text yet',
        '加入于': 'Added',
        '喜欢的书放在这里，下次打开直接接着上次的章节读。': 'Keep the books you love here and pick up where you left off.',        '设置': 'Settings',
        '退出登录': 'Sign out',
        '搜索书名、作者或出版社': 'Search title, author or publisher',
        '检索': 'Search',
        '搜索': 'Search',
        '图书板块': 'Collections',
        '最近上新': 'New Arrivals',
        '最近上架': 'Recently Added',
        '浏览图书板块': 'Browse Collections',
        '全馆检索': 'Search Library',
        '新建板块': 'New Collection',
        '新建图书板块': 'New Collection',
        '进入书架 →': 'Open Shelf →',
        '查看全部图书 →': 'View all books →',
        '查看全部 →': 'View all →',
        '编辑': 'Edit',
        '删除': 'Delete',
        '详情': 'Details',
        '保存': 'Save',
        '保存修改': 'Save changes',
        '取消': 'Cancel',
        '确认上架': 'Publish',
        '上架一本新书': 'Add a new book',
        '编辑图书信息': 'Edit book',
        '新增图书': 'Add book',
        '编辑图书': 'Edit book',
        '书名': 'Title',
        '作者': 'Author',
        '所属板块': 'Collection',
        '出版社': 'Publisher',
        '价格（元）': 'Price (CNY)',
        '库存（册）': 'Stock',
        '内容简介': 'Description',
        '选择板块': 'Choose a collection',
        '新建图书板块': 'New collection',
        '板块名称': 'Collection name',
        '排序权重': 'Sort order',
        '板块图标': 'Icon',
        '一句话简介': 'Short description',
        '主题色': 'Theme color',
        '创建板块': 'Create collection',
        '保存修改': 'Save changes',
        '返回书架': 'Back to shelf',
        '书架': 'Shelf',
        '编辑板块': 'Edit collection',
        '删除板块': 'Delete collection',
        '新增图书': 'Add book',
        '本板块内搜索...': 'Search this collection...',
        '最近上架': 'Newest',
        '最早上架': 'Oldest',
        '价格从低到高': 'Price: low to high',
        '价格从高到低': 'Price: high to low',
        '库存最多': 'Most stock',
        '书名 A-Z': 'Title A-Z',
        '上一页': 'Previous',
        '下一页': 'Next',
        '在馆书目': 'Books',
        '可阅读': 'Readable',
        '注册读者': 'Readers',
        '按主题整理的藏书空间，点击卡片进入对应书架': 'Browse the collection and open its shelf.',
        '刚刚收入书阁的三本书，点开封面就能开始阅读': 'Three recent arrivals. Open a cover to start reading.',
        '让每一本书都回到它的位置': 'A place for every book.',
        '墨卷书阁': 'Mojuan Library',
        '更换头像': 'Change avatar',
        '＋ 新建板块': '＋ New Collection',
        '· 让每一本书都回到它的位置': '· A place for every book.',
        '暂无数据': 'No data',
        '这个书架还没有书': 'This shelf is empty',
        '上架第一本书': 'Add the first book',
        '图书详情': 'Book details',
        '在架可借': 'Available',
        '暂时缺货': 'Out of stock',
        '上架时间': 'Added',
        '最近更新': 'Updated',
        '编辑图书': 'Edit book',
        '删除这本书': 'Delete this book',
        '开始阅读': 'Start reading',
        '继续阅读': 'Continue reading',
        '详情 / 打分': 'Details / Rate',
        '暂无评分': 'No ratings yet',
        '购买': 'Buy',
        '目录': 'Contents',
        '我的批注': 'My Notes',
        '书友批注': 'Readers’ Highlights',
        '本章评论': 'Chapter Comments',
        '自动滚动': 'Auto scroll',
        '慢速': 'Slow',
        '中速': 'Medium',
        '快速': 'Fast',
        '飞快': 'Very fast',
        '写下你的想法……（可以不填，仅做颜色标记）': 'Write your thoughts... (optional; color only is fine)',
        '保存批注': 'Save note',
        '仅颜色标记': 'Color only',
        '定位': 'Locate',
        '还没有批注，选中一段文字试试。': 'No notes yet. Select some text to add one.',
        '所有读者都可以看到你的评论': 'Everyone can see your comment',
        '发表评论': 'Post comment',
        '还没有人评论，来抢沙发。': 'No comments yet. Be the first.',
        '点赞': 'Like',
        '已赞': 'Liked',
        '回复': 'Reply',
        '删除': 'Delete',
        '第 1 章': 'Chapter 1',
        '本章评论': 'Chapter Comments',
        '发表评论': 'Post comment',
        '我的批注': 'My Notes',
        '选中正文文字即可添加彩色笔记；批注只有你自己能看见。': 'Select text to add a color note; notes are private to you.',
        '我的笔记': 'My Notes',
        '个人主页': 'Profile',
        '编辑个人资料': 'Edit profile',
        '昵称': 'Nickname',
        '邮箱': 'Email',
        '头像': 'Avatar',
        '个人简介': 'Bio',
        '保存资料': 'Save profile',
        '上传图书': 'Uploads',
        '批注笔记': 'Notes',
        '评分记录': 'Ratings',
        '最近笔记': 'Recent notes',
        '默认': 'Default',
        '账户与安全': 'Account & security',
        '修改密码后，下一次登录请使用新密码。': 'Use your new password the next time you sign in.',
        '当前密码': 'Current password',
        '新密码': 'New password',
        '确认新密码': 'Confirm new password',
        '更新密码': 'Update password',
        '界面语言': 'Language',
        '中文': 'Chinese',
        '简体中文界面': 'Simplified Chinese interface',
        'English': 'English',
        'English interface': 'English interface',
        '应用语言': 'Apply language',
        '阅读主题': 'Reading theme',
        '纸墨': 'Paper & Ink',
        '默认暖色': 'Default warm',
        '青竹': 'Bamboo',
        '护眼绿色': 'Eye-friendly green',
        '夜读': 'Night',
        '深色模式': 'Dark mode',
        '注销账户': 'Delete account',
        '输入密码确认注销': 'Enter password to confirm',
        '永久注销账户': 'Delete account permanently',
        '管理员账号不能自行注销。': 'Admin accounts cannot delete themselves.',
        '我的笔记': 'My Notes',
        '条笔记': 'notes',
        '还没有笔记': 'No notes yet',
        '回到原文 →': 'Back to text →',
        '查看图书 →': 'View book →',
        '仅颜色标记，没有文字笔记': 'Color only, no written note',
        '去找一本书': 'Find a book',
        '把你在书里划过的重点和写下的想法汇总在一起；同一段内容只保留最后一次的颜色和笔记。': 'All your highlights and notes in one place; repeated highlights keep the latest color and note.',
        '书友批注': 'Readers’ Highlights',
        '条': 'comments',
        '回复': 'Reply',
        '点赞': 'Like',
        '已赞': 'Liked',
        '删除': 'Delete',
        '暂无数据': 'No data'
    };

    var PATTERNS = [
        [/^共 (\d+) 种图书$/, '$1 books'],
        [/^共 (\d+) 条$/, '$1 comments'],
        [/^共 (\d+) 条评论$/, '$1 comments'],
        [/^(\d+) 条$/, '$1'],
        [/^(\d+) 条评论$/, '$1 comments'],
        [/^第 (\d+) 章$/, 'Chapter $1'],
        [/^上次读到第 (\d+) 章$/, 'Last read: Chapter $1'],
        [/^第 (\d+) \/ (\d+) 章$/, 'Chapter $1 / $2'],
        [/^第 (\d+) \/ (\d+) 页$/, 'Page $1 / $2'],
        [/^(\d+) 本藏书$/, '$1 books'],
        [/^库存 (\d+) 册$/, '$1 in stock'],
        [/^库存 (\d+)$/, 'In stock $1'],
        [/^(\d+) 人$/, '$1 people'],
        [/^本章 (\d+)%$/, 'Chapter $1%'],
        [/^已读 (\d+)%$/, 'Read $1%'],
        [/^第 (\d+) \/ (\d+) 章$/, 'Chapter $1 / $2']
    ];

    var SKIP = [
        'script', 'style', 'textarea', 'pre', 'code',
        '.no-i18n', '.book-title', '.book-author', '.book-cover', '.mini-info',
        '.cover-title', '.cover-cat', '.note-quote', '.note-text', '.anno-quote',
        '.anno-note', '.comment-body', '.anno-comment-body', '.my-note-quote',
        '.my-note-text', '.dropdown-head', '.flash-text',
        '.book-name', '.book-card', '.my-note-book', '.profile-bio', '.profile-handle',
        '.comment-content', '.anno-comment-content', '.user-content'
    ].join(',');

    function translate(value) {
        if (!value) {
            return value;
        }
        var key = value.trim();
        if (!key) {
            return value;
        }
        if (DICT[key]) {
            return value.replace(key, DICT[key]);
        }
        for (var i = 0; i < PATTERNS.length; i++) {
            var re = PATTERNS[i][0];
            if (re.test(key)) {
                return value.replace(key, key.replace(re, PATTERNS[i][1]));
            }
        }
        return value;
    }

    function shouldSkip(el) {
        if (!el || !el.closest) {
            return false;
        }
        return !!el.closest(SKIP);
    }

    function translateTextNodes(root) {
        var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
        var nodes = [];
        while (walker.nextNode()) {
            nodes.push(walker.currentNode);
        }
        nodes.forEach(function (node) {
            if (node.parentElement && !shouldSkip(node.parentElement)) {
                var next = translate(node.nodeValue);
                if (next !== node.nodeValue) {
                    node.nodeValue = next;
                }
            }
        });
    }

    function translateAttributes(root) {
        root.querySelectorAll('[placeholder]').forEach(function (el) {
            if (!shouldSkip(el)) {
                var value = translate(el.getAttribute('placeholder'));
                if (value) {
                    el.setAttribute('placeholder', value);
                }
            }
        });
        root.querySelectorAll('[title]').forEach(function (el) {
            if (!shouldSkip(el)) {
                var value = translate(el.getAttribute('title'));
                if (value) {
                    el.setAttribute('title', value);
                }
            }
        });
        root.querySelectorAll('[aria-label]').forEach(function (el) {
            if (!shouldSkip(el)) {
                var value = translate(el.getAttribute('aria-label'));
                if (value) {
                    el.setAttribute('aria-label', value);
                }
            }
        });
        root.querySelectorAll('option').forEach(function (el) {
            if (!shouldSkip(el)) {
                var value = translate(el.textContent);
                if (value) {
                    el.textContent = value;
                }
            }
        });
        root.querySelectorAll('input[type="submit"][value]').forEach(function (el) {
            var value = translate(el.value);
            if (value) {
                el.value = value;
            }
        });
    }

    function run() {
        document.documentElement.lang = 'en';
        translateTextNodes(document.body);
        translateAttributes(document);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', run);
    } else {
        run();
    }
})();
