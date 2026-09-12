-- 将本机刚初始化的模板资料改为金茂源；执行前已保存完整数据库备份。
START TRANSACTION;

-- 保持租户、部门和管理员编号，避免破坏账号与权限的关联。
UPDATE system_tenant SET name='金茂源', contact_name='金茂源管理员', contact_mobile='',
  websites='127.0.0.1:5173,localhost:5173'
  WHERE id=1 AND name='芋道源码';
UPDATE system_dept SET name='金茂源' WHERE id=100 AND name='芋道源码';
UPDATE system_users SET nickname='金茂源管理员', avatar='/jinmaoyuan-logo.png', email='', mobile='', remark='企业管理员'
  WHERE id=1 AND username='admin';

-- 移除原作者宣传入口及公告；采用系统已有的逻辑删除，备份可恢复。
UPDATE system_menu SET deleted=b'1', status=1, visible=b'0' WHERE id IN (194,347,348);
UPDATE system_notice SET title='欢迎使用金茂源管理系统', content='<p>欢迎使用金茂源管理系统。</p>' WHERE id=1;
UPDATE system_notice SET deleted=b'1' WHERE id=2 AND content LIKE '%iocoder%';

-- 清理初始化样例中的作者账号和联系方式，不改其他账号。
UPDATE system_users SET deleted=b'1', status=1 WHERE id IN (100,103,107,108,109,113)
  AND (nickname REGEXP '芋道|芋艿' OR email LIKE '%iocoder%');
UPDATE system_tenant SET websites='', contact_mobile='' WHERE id IN (121,122) AND websites LIKE '%iocoder%';
UPDATE system_tenant SET contact_name='测试联系人' WHERE id=122 AND contact_name='芋道';
UPDATE system_mail_template SET nickname='金茂源' WHERE id=14 AND nickname='芋艿';

-- 登录授权界面也采用企业名称与 Logo，移除旧作者的回调地址。
UPDATE system_oauth2_client SET name='金茂源管理系统', logo='/jinmaoyuan-logo.png',
  redirect_uris='["http://127.0.0.1:5173","http://localhost:5173"]' WHERE id=1;
UPDATE system_oauth2_client SET logo='/jinmaoyuan-logo.png' WHERE id IN (40,41,42) AND logo LIKE '%iocoder%';
UPDATE system_oauth2_client SET redirect_uris='[]' WHERE id=40 AND redirect_uris LIKE '%iocoder%';

COMMIT;
