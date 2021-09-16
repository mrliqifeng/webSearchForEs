
## 编写目的
最近在使用ElasticSearch（以下简称ES）搭建实时数仓的时候，对于索引中的数据都是编写代码或者查询语句进行搜索，效率低下。于是想着把搜索过程进行封装，并将搜索结果直接展示在前端。

**图1：数据列表页面**
![数据列表页面](https://img-blog.csdnimg.cn/20201221225245906.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21ybGlxaWZlbmc=,size_16,color_FFFFFF,t_70)
**图2：数据详情页面**
![数据详情页面](https://img-blog.csdnimg.cn/20201221225333143.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21ybGlxaWZlbmc=,size_16,color_FFFFFF,t_70)
**图3：数据聚合可视化页面**
![在这里插入图片描述](https://img-blog.csdnimg.cn/c88db5ba62654393b344cee1ec6be595.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5p2O5aWH5bOwMTk5OA==,size_20,color_FFFFFF,t_70,g_se,x_16)

## 功能介绍与亮点
1、用户可以直接在搜索框内输入数据，即可对ES库进行搜索，搜索结果将通过列表方式展示。其中列表字段可以自行配置（后续会讲具体配置方法）。

2、添加时间过滤与排序功能，用户可以直接在页面选定时间段对数据进行过滤与排序。前提是数据中需要存在时间字段并且进行配置。

3、数据分类展示，方便检索与查看相对应的数据。

4、高级搜索功能，可以在搜索词内输入“&&”或“||”进行“且与或”的逻辑搜索，相当于MySQL中的“or和and”。同时用户可以使用“people.name==李奇峰”即可指定搜索的具体字段。

5、用户可直接指定聚合字段，框架会自动将聚合结果进行可视化图表展示。

## 使用方法
1、将代码同步至本地后，找到application.yml并修改其中的配置，将数据替换成你本地的样式。
![在这里插入图片描述](https://img-blog.csdnimg.cn/6a5b8bf86b6f4c999f21c6839988e660.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5p2O5aWH5bOwMTk5OA==,size_20,color_FFFFFF,t_70,g_se,x_16)

2、代码同步后，找到search.sql文件并导入至mysql库中（需要提前建好库，库名随意），导入成功后出现三张表：table_info、field_info、agg_info。

**table_info用于控制那些index可以用于检索，并为index进行别名设置已经分类展示。表结构如下：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201221234407255.png)

**field_info用于控制检索列表展示字段。表结构如下：**

![!\[在这里插入图片描述\](https://img-blog.csdnimg.cn/2020122123464176.png](https://img-blog.csdnimg.cn/ddb7b465e3ba42cca668f2e68fbe54ad.png)

**agg_info用于控制index中的聚合字段用于可视化展示，表结构如下：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/a2a184ab10814b9287ad3a4d6fec52f8.png)

## 项目地址
[https://github.com/mrliqifeng/webSearchForEs](https://github.com/mrliqifeng/webSearchForEs)
## 感触
这个搜索引擎实现起来很快、难度不大，无非是对ElasticSearch接口的调用而已。编写的目的也只是为了服务于实时数仓，可以更快更方便的查看数仓中的数据。

但是在引擎的实现过程中，暴露出许多数据上的问题，比如**脏数据过多**导致搜索效率低下、**单条数据过大**导致http请求故障、**时间字段混乱**导致无法根据时间进行过滤与排序、**数据中的无用或低频字段过多**占用大量的索引空间，**过多的表**导致业务使用方开发效率低。

于是在实现搜索引擎的过程中，连带着做**数据治理，数据分层，数据整合**等工作。等到搜索引擎实现后，上面描述的数据问题已经基本解决。

最后的总结就是想要做出一个好的搜索引擎，或者说想要得心应手的使用数据，数据的治理工作占重中之重。
