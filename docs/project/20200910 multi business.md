### 组建新团队扩建业务？

新来一测试同事说他之前公司「组新团队扩展业务，功能大部分是copy」，受不了走了。。。而我们的部门就是这样，如今公司架构调整，部门打散，
一切回到原点，自己也去了其他部门，这里站在组织架构的角度上，思考下「组新团队扩展业务，功能大部分是copy」的相关的问题，并尝试做些总结。

#### 部门生命周期
1. 部门成立。公司做餐饮收银，18年（？）业务扩展，开辟了零售业务线，从各个团队抽人组队。成立了俩个团队：「pos端」、「商家后台」（我在）。
2. copy 相关业务，去餐饮（差异化修改）：屏蔽没有的业务；修改差异化的概念，如 子菜 =》子商品
3. 工程项目分离，独立迭代。
4. 日常 艰难 维护，「商家后台」开始征战其他团队（为其他团队开发代码。。。）
5. 后台全面征战其他部门
6. 公司架构调整，产品研发分离。研发不分餐饮、零售；产品好像要分（？），后续更新。

商家后台最开始维护了「零售商品」、「零售报表」、「零售设置」、「人效」（历史原因，中途划分出去）

#### 过程中问题

公司主力的产品形态是收银、设置、报表，而零售也需要这些，大差不差，很适合copy来修修改改 。。。从商户，运维到产品，很容易产生「这个功能我们也要」的想法，  
为了能够快速实现和推广，就开始了copy。。。。copy虽然有效，但维护成本大，特别是人少又要维护越来越多的copy。
一个团队own的业务是有边界的，遇到什么就copy，只会让团队越来越臃肿，后续维护难以为继。

当然，有时候新业务对老业务逻辑的破坏性不小，或者说不能随便兼容得到实现，相较于老业务相同优先级的需求，耗费研发成本可能翻倍甚至更多，很可能被拒或者延期，
但有一批自己的人就不一样了，copy也好，外包也好，这群人可以做，延期总比没有好。虽然这会导致后期维护成本高，但是团队得「活在当下」才有以后可言，如果真发展起来了，
后期有更多的人进来填坑也是可以缓解人力、维护的问题。

只是这过程往往会让产研疲于奔命。。。外包常常会打断研发节奏，因为不可控太多：  
产品可能考虑问题不够全面，而且需要耗费很多精力去梳理各个业务端的需求；  
研发常常蒙蔽，有时候功能调研就要几天，稍微考虑不周就是延期或者事故，无法快速迭代；有时候产品一句话："我们copy改改"，研发改断手；  
测试应该是最难受的，他们关注底层数据的正确性，但却没有代码，如果再没设计评审，就缺乏原始资料来源，往往只能页面点点点。。。  

时间久了，整个团队都累，但输出却不高。

一些常见的问题汇总：
1. 零售商户有报表问题，就算是底层业务有问题，餐饮同事也不排查问题，直接给权限给代码。。。。这不是个别同事的问题，就是部门职责的问题。

2. 产品也很累，他们要响应各个业务的需求，包括loyalty、设置、打印、商品，难有沉淀挖掘底层的业务需求；产品去其他团队提需求，部分团队没有零售业务的kpi，
需求响应不积极，常常以没有资源为由（当然也可能是知道零售有自己的团队），拒绝需求，有的甚至直接说他们是餐饮的部门，，，；

3. 各个团队有自己的研发资源，产品、研发、测试，他们有大致的平衡，当零售这个团队的产品对他们轰炸需求时，就可能打破这个平衡。

4. 需求优先级不够，因为商户量少，特别是刚开始的时候，很难获得较高的优先级。

5. 产品需求常常是让copy改改，工期还很短，或者一句话略过，"修改相关功能"。。。。这为后续维护又埋下了巨坑。后续每个周不得不排一人天的线上问题排查任务，迭代任务慢，

6. 整个分离实施中，因为沟通，考虑不周，自己团队、别人团队的原因都有，导致不少问题。因为组织架构变了，很多沟通都不便利。有沟，还挺大。

#### 探索实践
产研体系最大的kpi应该是需求响应速度，软件质量，软件的扩展性，而copy改改只能在前期得到大量响应速度的甜头，后期的需求响应速度、软件质量和扩展性在很大程度上
都依赖对产品/软件/代码的熟悉或者挖掘深度。再站在整个市场上，市场上那么多竞品，靠这样能打赢吗？

方案一：
就像商品和订单团队一样玩，不设新团队，在前期做大致方向的改版，满足基本可生产使用可销售，后期的需求就走正常需求流程，老的需求怎么来就怎么来，优先级就看公司重视程度。
不过每个团队但有需要有人owner这个事情。

暂时表面总结一下，有些东西还是没想透，比如各团队有人owner，执行情况会怎么样？不想花太多时间在这上面。。。以后再看吧。


