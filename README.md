# Luban

[![](https://jitpack.io/v/4evercai//Luban.svg)](https://jitpack.io/#4evercai/Luban)
[![License](https://img.shields.io/badge/Apache-License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Blog](https://img.shields.io/badge/site-youquan.pro-9932CC.svg)](http://youquan.pro)


[Curzibn/Luban](https://github.com/Curzibn/Luban) Kotlin版本，增加压缩图片质量设置参数

## 引入
   Gradle
   1. 在项目 build.gradle 添加 JitPack 仓库
   ```kotlin
    allprojects {
        repositories {
            ... 
            maven { url 'https://jitpack.io' }
        }
    }
```
   2. 引入库
   ```kotlin
    // 其中latest.release指代最新版本号，也可以指定明确的版本号，例如1.2.0
    compile 'com.github.4evercai:Luban:latest.release'  
``` 

     
# 使用

新增Luban.Builder.compressQuality(compressQuality: Int)方法，其他的与[Curzibn/Luban](https://github.com/Curzibn/Luban)类似

# License

    Copyright 2018 4evercai
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.