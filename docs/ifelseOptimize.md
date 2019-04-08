# 为什么要有这个话题？
 在我们开发业务的过程中，都经历过这样的场景：最初的代码都很简洁，逻辑也很清晰，基本没有if-else。随着业务变化、逻辑越来越复杂，就需要做不同的类型进行不同的程序处理，如果没有及时优化，就会出现if-else的不停叠加，渐渐地代码越来越长，类越来越大，维护难度越来越大
 
# 如何优化？
## 简化if-else里面的逻辑块，封装成一个公共方法，代码结构和逻辑更清晰，
能清楚的看出来每个分支要做的具体功能当判断条件和分支逻辑复杂时，使用分解条件表达式 ，突出条件逻辑，更清楚地表名每个分支的作用，并且突出每个分支的原因，
减少理解成本，清晰分支逻辑，简化主流程。

```java
public class DecomposeConditional {

    private static boolean b1 = true;
    private static boolean b2 = true;
    private static boolean b3 = true;

    private static void optimize() {
        if (firstSituation()) {
            // doFirstStep
        } else if (secondSituation()) {
            // dSecondStep
        } else if (thirdSituation()) {
            // doThirdStep
        } else {
            // doElse
        }
    }

    private static void badSmell() {
        if (b1 && b2 || b3) {
            // doSth1
            // doSth2
            // doSth3
        } else if (b3 && b2 | b1) {
            // doSth4
            // doSth5
            // doSth6
        } else if (b1 & b2 & b3) {
            // doSth7
            // doSth8
            // doSth9
        } else {
            // doSth0
        }
    }

    private static boolean firstSituation(){
        return b1 && b2 || b3;
    }
    private static boolean secondSituation(){
        return b3 && b2 | b1;
    }
    private static boolean thirdSituation(){
        return b1 & b2 & b3;
    }
}
```

## 当有一些列的条件表达式，有多处返回有相同结果时，使用合并表达式
```java
/**
 * 合并条件表达式
 */
public class ConsolidateConditionalExpression {
    private static boolean b1 = true;
    private static boolean b2 = true;
    private static boolean b3 = true;
    
    private static Integer badSmell() {
        if (b1) {
            return 0;
        }

        if (b2) {
            return 0;
        }

        if (b3) {
            return 0;
        }
        // doSth
        return 1;
    }
    private static Integer optimize() {
        if (b1 || b2 || b3) {
            return 0;
        }
        // doSth
        return 1;
    }
    
}
```

## 当分支逻辑中，有重复代码段，可以 合并重复的片段

```java
/**
 * 合并重复的条件片段
 */
public class ConsolidateDoplicateConditionFragments {
    private static boolean b1 = true;
    private static boolean b2 = true;
    private static boolean b3 = true;

    private static void badSmell() {
        if (b1) {
            // doSth1
            // doSth
        } else {
            // doSth2
            // doSth
        }
    }

    private static void optimize() {
        if (b1) {
            // doSth1
        } else {
            // doSth2
        }
        // doSth
    }
}
```

## 在一系列布尔表达式中，某个变量带有“控制标记”的作用，以break语句或者return语句取代控制标记
```java
/**
 * 移除控制标记
 */
public class RemoveControlFlag {

    private static boolean b1 = true;
    private static boolean b2 = true;
    private static boolean b3 = true;

    static void badSmell() {
        boolean found = false;
        for (int i = 0; i < 10; i++) {
            if (!found) {
                if (b1) {
                    // doSth1
                    found = true;
                }
                if (b2) {
                    // doSth2
                    found = true;
                }
            }
        }
    }
    static void optimize() {
        for (int i = 0; i < 10; i++) {
            if (b1) {
                // doSth1
                break;
            }
            if (b2) {
                // doSth2
                break;
            }
        }
    }
}
```

## 以卫语句取代嵌套条件表达式 （快速返回）
卫语句定义：它的核心思想是，将不满足某些条件的情况放在方法前面，并及时跳出方法，以免对后面的判断造成影响，经过这项手术的代码看起来会非常的清晰。
```java
class Demonstration {
    
    // 待优化
    public void function() {
        if(boolean1) {
            // doSth1
        } else {
            if(boolean2) {
                // doSth2
            } else {
                // doSth3
            }
        }
    }
    
    // 优化后
    public void function(){
        if(boolean1) {
            // doSth1
            return;
        }
        if(boolean2) {
            // doSth2
            return;
        }
        // doSth3
    }
}
```
    
## 多态，面向对象的抽象
对行为进行抽象，抽出公共行为，多态之后，往往需要处理映射，
- 明确映射关系，使用map，例子：
com.fasterxml.jackson.databind.ser.BasicSerializerFactory#_concrete
com.fasterxml.jackson.databind.ser.BasicSerializerFactory#_concreteLazy

- 不明确映射关系，遍历。例如：springmvc源码中用于匹配对应的参数处理器：
见：org.springframework.web.method.support.InvocableHandlerMethod#getMethodArgumentValues

## 需要再三检查某对象是否为null， 为null时，需要获取特殊值，或者特殊处理，可以 引入Null对象

```java

interface Strategy {

    void doSth();

    void doSth1();

    void doSth2();
}

class StrategyUse {

    Strategy strategy;

    public StrategyUse(Strategy strategy) {
        this.strategy = strategy;
    }

    public void doSth() {
        if (this.strategy != null) {
            strategy.doSth();
        } else {
            System.out.println("doSth while null");
        }
    }

    public void doSth1() {
        if (this.strategy != null) {
            strategy.doSth1();
        } else {
            System.out.println("doSth1 while null");
        }
    }

    public void doSth2() {
        if (this.strategy != null) {
            strategy.doSth2();
        } else {
            System.out.println("doSth2 while null");
        }
    }
}


class NullStrategy implements Strategy {

    @Override
    public void doSth() {
        System.out.println("doSth while null");
    }

    @Override
    public void doSth1() {
        System.out.println("doSth1 while null");
    }

    @Override
    public void doSth2() {
        System.out.println("doSth2 while null");
    }
}

class StrategyUseOptimize{

    Strategy strategy;

    public StrategyUseOptimize(Strategy strategy) {
        this.strategy = strategy == null? new NullStrategy(): strategy;
    }

    public void doSth() {
        strategy.doSth();
    }

    public void doSth1() {
        strategy.doSth1();
    }

    public void doSth2() {
        strategy.doSth2();
    }
}
```

# 总结分析
if-else代码是最容易写的代码，也是最容易写出code smell，稍不注意，就会让代码逻辑混乱，难以维护。可以根据以上重构方法，以有效的减少if-else语句，减少嵌套逻辑来优化代码。
      
# 参考：
[减少该死的 if else 嵌套](https://mp.weixin.qq.com/s/0kQLS9hp2VymzEg5SEf_MQ)
[Java中避免if-else-if：策略模式](https://www.jianshu.com/p/71feb016ac05)
《重构-改善既有代码的设计》第九章(老版本)