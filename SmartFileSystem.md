# SmartFileSystem

<p align ="right">刘俊伟 18302010042</p>

## 一、系统结构

## 二、异常处理

#### ErrorCode.FILE_NOT_FOUND && ErrorCode.IO_EXCEPTION

Block.read() ->  File.readBlock() -> File.read() ->Util.smart_cat()

##### 异常包装

##### 1. 避免用户通过异常发现系统内部实现，这只要实现一些上层错误就可以

2. 打断上扔过程，做出一些处理，可以不实现大的错误，也可以不用包装，继续上扔就是了



用户可以看到的异常：

**create**

* **DUPLICATE_FILENAME**

  当用户在同一个fileManager里使用了重复的文件名时会报此异常。

* **FILE_NAME_OCCUPIED**

  当用户指定的fileManager中存在一个以fileName命名的文件夹时报此异常。

* **IO_EXCEPTION**

  系统创建此文件并向其中写入信息发生IOException时报此异常。

* **INITIAL_FILE_FAILED**

  在对文件系统进行初始化时，文件的元数据文件出现问题后者读取元文件时出现问题。



* CURSOR_OUT_OF_RANGE
* FILE_NOT_FOUND





Block链上：只有BlockManager在newBlock的时候拦截了一下，为的是删除失败的文件，之后继续上扔了。

File链上：

MyFile初始化时拦截一次，包装成FILE_INITIAL_FAILED



### 三、垃圾回收机制