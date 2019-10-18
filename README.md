# MYSQL EXPLAIN
auto execute mysql explain when execute sql

## Main functions
- auto print mysql explain result
- can print original sql
- more simple filters

## 功能描述
- 自动输出SQL语句的执行计划
- 自动输出执行的SQL语句
- 以上两者不重复，可设置过滤条件

## 相关配置描述如下：

```properties
# 非必填项：是否打印所有执行的MySQL语句，默认false。设置true时，会根据[mysql.filter]过滤滤
mysql.showSQL=false

# 非必填项：MySQL explain执行过滤，按关键词匹配，英文逗号分割，比如：QRTZ_,COUNT(0)
mysql.filter=QRTZ_,COUNT(0)

# 非必填项：MySQL explain结果按[type]过滤，默认ALL，英文逗号分割，*打印所有
mysql.types=ALL

# 非必填项：MySQL explain结果按[Extra]过滤，默认Using filesort,Using temporary，英文逗号分割，*打印所有
mysql.extras=Using filesort,Using temporary
```