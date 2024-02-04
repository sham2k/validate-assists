# 验证辅助工具

本工具增强HibernateValidator的功能，支持以`Map`对象的`key`作为`field`定义约束，对`Map`对象的值进行验证。本工具典型应用场景是通过Map传递数据以减少值对象类数量和实现通用化通信时验证数据合法性。

## 1.快速入门

### 1.1 添加依赖

````
<dependency>
  <groupId>com.github.sham2k</groupId>
  <artifactId>validate-assists</artifactId>
  <version>1.0.0</version>
</dependency>
````

### 1.2 配置验证约束

本工具通过自定义校验器（`@ValueMap`）扩展 HibernateValidator 功能，因此需在相应字段上添加约束注解，或通过XML文件定义相应字段的约束，具体定义方法参考 HibernateValidator 文档。

#### 1.2.1 待验证的类

````
@Data
public class WebReq
{
    private String cmdCode;
    private Map<String, Object> reqData;
}
````
* `cmdCode`: 请求编号，服务端据此判定`reqData`的具体内容和要调用的服务。
* `reqData`: 请求数据，这是由本工具验证的对象，其内容是动态变化的。

#### 1.2.2 WebReq 约束配置
`WebReq`使用HibernateValidator标准配置，可以使用注解或XML文件配置，并由HibernateValidation读取与处理。

##### (1) 注解模式

````
public class UserReq
{
    @NotNull
    @Length(min = 7, max = 7, message = "cmdCode长度应是xxx.nnn格式！")
    private String cmdCode;

    @ValueMap(defineName = "cmd.001")
    private Map<String, Object> reqData;
}

````

##### (2) XML文件模式
使用标准的HibernateValidator XML 配置文件，且应按HibernateValidator要求存放到相应的目录。
````
<constraint-mappings
        xmlns="https://jakarta.ee/xml/ns/validation/mapping"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://jakarta.ee/xml/ns/validation/mapping
            https://jakarta.ee/xml/ns/validation/validation-mapping-3.0.xsd"
        version="3.0">

    <bean class="com.github.sham2k.test.bean.WebReq" ignore-annotations="false">
        <field name="cmdCode">
            <constraint annotation="org.hibernate.validator.constraints.Length">
            ...
            </constraint>
        </field>
        <field name="reqData">
            <constraint annotation="com.github.sham2k.validation.constraints.ValueMap">
                <element name="defineName">
                    <value>cmd.001</value>
                </element>
            </constraint>
        </field>
    </bean>
</constraint-mappings>
````
* 本例在`reqData`字段定义`ValueMap`约束，指示该字段由本工具进行验证。
* `reqData`字段的组成项及约束定义，由名为`defineName`的元素值确定，本例中为`cmd.001`。本工具自动从各约束配置中查找名为`cmd.001`的约束集合，并使用它对`reqData`进行验证。

#### 1.2.3 ReqData 约束配置
由于`ReqData`的组成内容是变化的，无法使用注解模配置，只能使用XML文件配置。XML配置文件可以存放在类路径中或外部配置目录中，并由本工具进行读取和使用。
````
<constraint-mappings>
    <bean class="cmd.001" ignore-annotations="false">
        <field name="table">
            <constraint annotation="org.hibernate.validator.constraints.Length">
                <element name="min">
                    <value>0</value>
                </element>
                <element name="max">
                    <value>60</value>
                </element>
            </constraint>
        </field>
        <field name="rows">
            <constraint annotation="jakarta.validation.constraints.NotNull">
                <groups>
                    <value>
                        com.github.sham2k.test.group.SELECT
                    </value>
                </groups>
            </constraint>
        </field>
        <field name="user">
            <valid/>
        </field>
    </bean>
</constraint-mappings>
````
* 本文件格式和标准的HibernateValidator文件一致，只是目前没有包含`xmlns`等文件描述信息。
* 一个文件可以定义一个或多个`bean`，并通过`class`属性定义约束集合的名称。该名称应全局唯一，且和`WebReq`文件的定义一致。本工具据此确定要使用哪个约束集合对`reqData`进行验证。
* `bean`的组成字段是变化的，由业务规则确定。
* 本例中，`user`字段是对象，通过`<valid/>`指示由HibernateValidator进行验证。

### 1.3 初始化
初始化主要包括初始化验证工厂和读取定义的验证文件两个操作。执行初始化操作的时机通常在启动应用程序后且受理请求前进行，具体时机由应用程序自行确定。

#### 1.3.1 初始化验证工厂
应用程序自行初始化验证工厂，并通过如下代码将验证工厂实例传递给本工具。本工具通过验证工厂获取系统定义的验证器及相关参数进行相关验证处理。
````
ValidatorManager.setValidatorFactory(validatorFactory);

````
#### 1.3.2 读取验证文件
通过如下代码读取验证文件。本工具将读取的验证文件内容存储在内存中备用(类似全局变量)。
````
ConfigManager.loadConfig("/app/config", "validation-cfg");

````
* `config`: 配置外置时，为存储配置文件的具体目录。如没有使用配置外置，本参数可以设置为`null`或`""`。如该参数值为`null`，本工具自动尝试从环境变量`APP_CFG_HOME`和`CONFIG_HOME`获取值作为本参数的值。
* `validation-cfg`: 配置文件名标识。本工具自动扫描类路径和配置目录下文件名前缀是该标识或目录名是该标识的`.xml`文件。例如：`validation-cfg/*.xml`、`validation-cfg*.xml`。

### 1.4 执行验证
使用 HibernateValidator 标准用法执行验证。验证过程中 HibernateValidator 自动调用本工具进行额外验证。
如应用程序启用`group`验证，由于本工具无法从 HibernateValidator 获取当前验证的`groups`，因此应在启动验证前，通过如下代码设置当前启用的`groups`。
````
ValidatorManager.setGroups(Default.class, SELECT.class);

````
或使用本工具作为验证过程的入口。例如：
````
Set<ConstraintViolation<WebReq>> result = ValidatorManager.validate(req, SELECT.class);
````
本工具将当前启用的`groups`存储在`ThreadLocal`变量中备用，并在验证结束时释放该数据。如期望主动释放该数据，可以使用如下代码：
````
ValidatorManager.setGroups(null);
````

## 2.高级用法
### 2.1 自动判定约束集合名


