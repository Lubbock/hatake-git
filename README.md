# hatake-git

`Git`的java版本实现，实现原理依据文章:[源码解析：Git的第一个提交是什么样的](https://zhuanlan.zhihu.com/p/257084586)

[toc]

## index文件
 `index`中，文件变更记录uid,ino改为8byte,所有的size都改成了long 类型

## 结束语
目前hatake-git 开发已经画上了休止符。git初版的核心对象，除commit外，其他读取解析，基本实现。了解，锻炼了randomAccessFile的基本用法。
更深入的学习了二进制相关知识。commit对象目前没有继续做下去的必要性。所以后续开发终止。等待以后有实际使用场景的时候继续开发