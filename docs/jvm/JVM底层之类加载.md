### klass模型
   
   Java的每个类，在JVM中，都有一个对应的Klass类实例与之对应，存储类的元信息如：常量池、属性信息、方法信息……
   
   看下klass模型类的继承结构
   
   ![](jvm.assets/Klass.png)
   
   从继承关系上也能看出来，类的元信息是存储在原空间的
   
   普通的Java类在JVM中对应的是instanceKlass类的实例，再来说下它的三个字类
    
    1. InstanceMirrorKlass：用于表示java.lang.Class，Java代码中获取到的Class对象，实际上就是这个C++类的实例，存储在堆区，学名镜像类
    2. InstanceRefKlass：用于表示java/lang/ref/Reference类的子类
    3. InstanceClassLoaderKlass：用于遍历某个加载器加载的类
   
   Java中的数组不是静态数据类型，是动态数据类型，即是运行期生成的，Java数组的元信息用ArrayKlass的子类来表示：
   
    1. TypeArrayKlass：用于表示基本类型的数组
    2. ObjArrayKlass：用于表示引用类型的数组
    
### 类加载的过程
   
   类加载由7个步骤完成，看图
   
   类的生命周期是由7个阶段组成，但是类的加载说的是前5个阶段
   
   ![]()
   
#### 加载
   
   1、通过类的全限定名获取存储该类的class文件（没有指明必须从哪获取）
   
   2、解析成运行时数据，即instanceKlass实例，存放在方法区
   
   3、在堆区生成该类的Class对象，即instanceMirrorKlass实例
   
   程序随便你怎么写，随便你用什么语言，只要能达到这个效果即可
   
   就是说你可以改写openjdk源码，你写的程序能达到这三个效果即可
   
   何时加载?
    
    主动使用时
    1、new、getstatic、putstatic、invokestatic
    2、反射
    3、初始化一个类的子类会去加载其父类
    4、启动类（main函数所在类）
    5、当使用jdk1.7动态语言支持时，如果一个java.lang.invoke.MethodHandle实例最后的解析结果REF_getstatic,REF_putstatic,REF_invokeStatic的方法句柄，并且这个方法句柄所对应的类没有进行初始化，则需要先出触发其初始化
   
   预加载：包装类、String、Thread
   
   因为没有指明必须从哪获取class文件，脑洞大开的工程师们开发了这些
   
    1、从压缩包中读取，如jar、war
    2、从网络中获取，如Web Applet
    3、动态生成，如动态代理、CGLIB
    4、由其他文件生成，如JSP
    5、从数据库读取
    6、从加密文件中读取
    
#### 验证
   
   1、文件格式验证
   2、元数据验证
   3、字节码验证
   4、符号引用验证
   
#### 准备
   
   为静态变量分配内存、赋初值
   
   实例变量是在创建对象的时候完成赋值的，没有赋初值一说
   
   ![]()
   
#### 解析
   
   将常量池中的符号引用转为直接引用
   
   解析后的信息存储在ConstantPoolCache类实例中
    
    1、类或接口的解析
    2、字段解析
    3、方法解析
    4、接口方法解析
    
   何时解析?
    
    思路：
    1、加载阶段解析常量池时
    2、用的时候
    openjdk是第二种思路，在执行特定的字节码指令之前进行解析：
        anewarray、checkcast、getfield、getstatic、instanceof、invokedynamic、invokeinterface、invokespecial、invokestatic、invokevirtual、ldc、ldc_w、ldc2_w、multianewarray、new、putfield
        
#### 初始化
   
   执行静态代码块，完成静态变量的赋值
   
   静态字段、静态代码段，字节码层面会生成clinit方法
   
   方法中语句的先后顺序与代码的编写顺序相关
   
### 读取静态字段的实现原理
   
```
public class Test_1 {
    public static void main(String[] args) {
        System.out.printf(Test_1_B.str);
    }
}

class Test_1_A {
    public static String str = "A str";

    static {
        System.out.println("A Static Block");
    }
}

class Test_1_B extends Test_1_A {
    static {
        System.out.println("B Static Block");
    }
}
```
   
   静态字段如何存储
   
   instanceKlass
   
   instanceMirrorKlass
   
   Test_1_A的
   
   ![]()
   
   静态变量str的值存放在StringTable中，镜像类中存放的是字符串的指针
   
   Test_1_B
   
   ![]()
   
   str是类Test_1_A的静态属性，可以看到不会存储到子类Test_1_B的镜像类中
   
   可以猜得到，通过子类Test_1_B访问父类Test_1_A的静态字段有两种实现方式：
   
    1、先去Test_1_B的镜像类中去取，如果有直接返回；如果没有，会沿着继承链将请求往上抛。很明显，这种算法的性能随继承链的death而上升，算法复杂度为O(n)
    2、借助另外的数据结构实现，使用K-V的格式存储，查询性能为O(1)
   
   Hotspot就是使用的第二种方式，借助另外的数据结构ConstantPoolCache，常量池类ConstantPool中有个属性_cache指向了这个结构。每一条数据对应一个类ConstantPoolCacheEntry。
   
   ConstantPoolCacheEntry在哪呢？在ConstantPoolCache对象后面，看代码
   
   \openjdk\hotspot\src\share\vm\oops\cpCache.hpp
   
```
ConstantPoolCacheEntry* base() const           { 
  return (ConstantPoolCacheEntry*)((address)this + in_bytes(base_offset()));
}
```
   
   这个公式的意思是ConstantPoolCache对象的地址加上ConstantPoolCache对象的内存大小
   
   *ConstantPoolCache*
   
   常量池缓存是为常量池预留的运行时数据结构。保存所有字段访问和调用字节码的解释器运行时信息。缓存是在类被积极使用之前创建和初始化的。每个缓存项在解析时被填充
   
   *如何读取*
   
   \openjdk\hotspot\src\share\vm\interpreter\bytecodeInterpreter.cpp
   
```
CASE(_getstatic):
        {
          u2 index;
          ConstantPoolCacheEntry* cache;
          index = Bytes::get_native_u2(pc+1);

          // QQQ Need to make this as inlined as possible. Probably need to
          // split all the bytecode cases out so c++ compiler has a chance
          // for constant prop to fold everything possible away.

          cache = cp->entry_at(index);
          if (!cache->is_resolved((Bytecodes::Code)opcode)) {
            CALL_VM(InterpreterRuntime::resolve_get_put(THREAD, (Bytecodes::Code)opcode),
                    handle_exception);
            cache = cp->entry_at(index);
          }
……
```
  
   从代码中可以看出，是直接去获取ConstantPoolCacheEntry
   
   