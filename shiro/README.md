# Shiro

> subject.login 自动委托给 SecurityManager.login 方法进行登录;
>
> subject.logout 自动委托给 SecurityManager.logout 方法进行退出;

#### 常见异常

+ AuthenticationException 身份验证失败
  + DisabledAccountException 禁用的帐号
  + LockedAccountException 锁定的帐号
  + UnknownAccountException 错误的帐号
  + ExcessiveAttemptsException 登录失败次数过多
  + IncorrectCredentialsException 错误的凭证
  + ExpiredCredentialsException 过期的凭证

#### AuthenticationStrategy

+ FirstSuccessfulStrategy
  + 只要有一个Realm验证成功即可，只返回第一个Realm身份验证成功的认证信息
+ AtLeastOneSuccessfulStrategy
  + 只要有一个Realm验证成功即可，返回所有Realm身份验证成功的认证信息
+ AllSuccessfulStrategy
  + 所有Realm验证成功才算成功，且返回所有Realm身份验证成功的认证信息





### TODO

1. 每个请求, 刷新token时间
2. 动态更新权限信息/登录信息

controller 获取当前用户

jsession存入redis*

*































xie shenyuan

tui heixia

