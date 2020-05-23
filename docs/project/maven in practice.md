### maven 常见问题合集

#### jar 包管理
目前项目jar包管理存在的问题：
- 各种jar包都在一个库中，每升级或者加一个小功能，都需要重新打包。所以需要将关联紧密的包放在一起。
- relativePath 全用默认，但parent不是上级module，导致难打包
- 目前线上使用的都是snapShot包，存在被新版本影响的风险
- 有同事提过开发使用snapshot包，线上使用Release包，这样会不会很麻烦？
- 最近线上发现一个问题，想复现问题，但是无法排查jar包的影响，如果我们采用每次快照小改动都用相同包，那就会影响问题排查。
- pom包升级版本号一定要在提交日志中体现，方便排查


打包控制
  - 通过versions-maven-plugin 插件控制，咨询同事控制逻辑
    - mvn versions:set -DnewVersion=${RELEASE_VERSION}
    - mvn clean install source:jar -N -DskipTests=true
    - mvn clean install source:jar deploy -DskipTests=true
  - 通过profile来控制打release还是打snapshot包，mybatis-spring版本控制 ：：TODO


#### 额外知识

- dependencyManagement

- relativePath 默认是 ../pom.xml 用于寻找parent的pom文件地址

- [optional](https://juejin.im/post/5dc0c36be51d456e35627114)

- 依赖版本的原则：路径最短者优先原则、路径相同先声明优先原则


远程仓库的配置
```
<repositories>
    <repository> <repository>
        <id>kry-nexus</id>
        <url></url>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository>
    <repository>
        <id>kry-nexus-snapshots</id>
        <url></url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
        </snapshots>
    </repository>
</repositories>
```

构建发布
```
<distributionManagement>
    <repository>
        <id>releases</id>
        <name>Local Nexus Repository</name>
        <url></url>
    </repository>
    <snapshotRepository>
        <id>snapshots</id>
        <name>Local Nexus Repository</name>
        <url></url>
    </snapshotRepository>
</distributionManagement>
```

- 现有项目中settings配置
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
		<server>
			<id>snapshots</id>
			<username></username>
			<password></password>
		</server>
		<server>
			<id>releases</id>
			<username></username>
			<password></password>
		</server>
	</servers>

	<mirrors>
		<mirror>
			<id>kry-nexus</id>
			<name>internal nexus repository</name>
			<url></url>
			<mirrorOf>central</mirrorOf>
		</mirror>
	</mirrors>
</settings>

- 同事经搜索后推荐的配置，因为在上面的配置中，偶发拉不到spring 包的情况，我没有遇到过。

<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>mirror</id>
            <mirrorOf>!kry-nexus,!kry-nexus-snapshots,!rdc-releases,!rdc-snapshots</mirrorOf>
            <name>mirror</name>
            <url>https://maven.aliyun.com/nexus/content/groups/public</url>
        </mirror>
    </mirrors>

    <servers>
        <server>
            <id>rdc-releases</id>
            <username></username>
            <password></password>
        </server>
        <server>
            <id>rdc-snapshots</id>
            <username></username>
            <password></password>
        </server>
    </servers>
    
    <profiles>
        <profile>
            <id>nexus</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://maven.aliyun.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                
                <repository>
                    <id>snapshots</id>
                    <url>https://maven.aliyun.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>false</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
                
                <repository>
                    <id>rdc-releases</id>
                    <url></url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                
                <repository>
                    <id>rdc-snapshots</id>
                    <url></url>
                    <releases>
                        <enabled>false</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
            
            <pluginRepositories>
            
                <pluginRepository>
                    <id>central</id>
                    <url>https://maven.aliyun.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </pluginRepository>
                
                <pluginRepository>
                    <id>snapshots</id>
                    <url>https://maven.aliyun.com/nexus/content/groups/public</url>
                    <releases>
                        <enabled>false</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </pluginRepository>
                
                <pluginRepository>
                    <id>rdc-releases</id>
                    <url></url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </pluginRepository>
                
                <pluginRepository>
                    <id>rdc-snapshots</id>
                    <url></url>
                    <releases>
                        <enabled>false</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>

    </profiles>

    <activeProfiles>
        <activeProfile>nexus</activeProfile>
    </activeProfiles>


</settings>


[好文](https://github.com/landy8530/fortune-commons/wiki/Maven-Version-Control)

