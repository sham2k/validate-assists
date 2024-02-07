# Validate-Assists

This tool enhances the functionality of HibernateValidator by supporting define constraints on the Map's keys and validate Map's values. The typical application scenario is to transfer data through a Map object and validate the validity of data during universal communication.

## 1 Getting Started

### 1.1 Add Dependency

````
<dependency>
  <groupId>com.github.sham2k</groupId>
  <artifactId>validate-assists</artifactId>
  <version>0.2.4</version>
</dependency>
````

### 1.2 Configure Validation Constraints

This tool extends the HibernateValidator function through a custom validator (`@ValueMap`). Therefore, constraint annotations need to be added to the corresponding fields, or the constraints of the corresponding fields need to be defined through an XML file. Please refer to the specific definition method for reference HibernateValidator documentation.

#### 1.2.1 Class to be valudated

````
@Data
public class WebReq
{
    private String cmdCode;
    private Map<String, Object> reqData;
}
````

* `cmdCode`: The request number is used by the server to determine the specific content of 'reqData' and the service to be called.
* `reqData`: Request data, which is the object validated by this tool, and its content is dynamically changing.

#### 1.2.2 WebReq Constraint Configuration

`WebReq uses HibernateValidation standard configuration, which can be configured using annotations or XML files and read and processed by HibernateValidation.

##### (1) Annotation Mode

````
public class UserReq
{
    @NotNull
    @Length(min = 7, max = 7, message = "The length of cmdCode should be in xxx.nnn format")
    private String cmdCode;

    @ValueMap(defineName = "cmd.001")
    private Map<String, Object> reqData;
}

````

##### (2) XML File Mode

Use standard HibernateValidator XML configuration files and store them in the corresponding directory according to HibernateValidator requirements.

````
<constraint-mappings
        xmlns="https://jakarta.ee/xml/ns/validation/mapping"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://jakarta.ee/xml/ns/validation/mapping
            https://jakarta.ee/xml/ns/validation/validation-mapping-3.0.xsd"
        version="3.0">

    <bean class="bean.io.github.sham2k.test.WebReq" ignore-annotations="false">
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

* This example defines a 'Value Map' constraint on the 'reqData' field, indicating that the field is validated by this tool.
* The contents and constraint definitions of the reqData field are determined by the value of an element named 'defineName', which in this case is 'cmd.001'. This tool automatically searches for a constraint set named 'cmd. 001' from various constraint configurations and uses it to validate 'reqData'.

#### 1.2.3 ReqData Constraint Configuration

Due to the variable content of 'ReqData', it cannot be configured using annotation constraint and can only be configured using XML files. XML configuration files should be stored in class paths or external configuration directories, and can be read and used by this tool.

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
                        group.io.github.sham2k.test.SELECT
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

* This file format is consistent with the standard HibernateValidator file, but currently does not include file description information such as 'xmlns'.
* One file can define one or more 'beans' and define the name of the constraint set through the 'class' attribute. The constraint set name should be globally unique and consistent with WebReq's definition. This tool determines which constraint set to use to validate 'reqData' based on this.
* The composition fields of bean are variable and determined by business rules.
* In this example, the 'user' field is an object instance, indicated by '<valid/>' to be validated by HibernateValidator.

### 1.3 Initialization

Initialization mainly includes two operations: initializing the validation factory and reading the defined constraint file. The timing of the initialization operation is usually determined by the application itself, after starting the application and before accepting the request.

#### 1.3.1 Initialize Validation Factory

The application initializes the validation factory on its own and passes the validation factory instance to this tool through the following code. This tool obtains system defined validators and related parameters through factory validation for relevant validation processing.

````
ValidatorManager.setValidatorFactory(validatorFactory);

````

#### 1.3.2 Read Constraint Definition File

Read the constraint definition file through the following code. This tool stores the read file content in memory (similar to global variables).

````
ConfigManager.loadConfig("/app/config", "validation-cfg");

````

* `config`: When using external configuring file, this parameter is the directory where the configuration files are stored. If external configuration file is not used, this parameter can be set to `null` or `""`. If the parameter value is `null`, this tool automatically attempts to access the environment variable `APP_ CFG_ HOME` and `CONFIG_ HOME` value as the value of this parameter.
* `validation-cfg`: Configuration file name identification. This tool automatically scans the file name prefix in the class path and configuration directory for all XML file, which filename is the same identifier or directory name is the same identifier.  File. For example:`validation-cfg/*.xml`、`validation-cfg*.xml`。

### 1.4 Validation Process

Perform validation using HibernateValidator standard usage. During the validation process, HibernateValidator automatically calls this tool for additional validate. If the application enables `group` validation, this tool cannot retrieve the currently validated groups from HibernateValidator`. Therefore, before starting the validation process, the currently enabled groups should be set using the following code.

````
ValidatorManager.setGroups(Default.class, SELECT.class);

````

Or use this tool as an entry point for the validation process. For example:

````
Set<ConstraintViolation<WebReq>> result = ValidatorManager.validate(req, SELECT.class);
````

This tool stores the currently enabled groups in the ThreadLocal variable and releases at the end of validation. To actively release the data as expected, the following code can be used:

````
ValidatorManager.setGroups(null);
````

## 2 Typical Uses

### 2.1 Automatically Determine Constraint Set Names

This tool supports using `${xxx}` placeholder indicates obtains attribute values from the current object as the name of the constraint set name to simplify constraint configuration, suitable for scenarios where Map is used to transfer data in multiple formats< Br>
For example:

#### 2.1.1 Annotation Mode

````
@Data
@ValueMap(defineName = "${cmdCode}", targetName = "reqData")
public class WebReq4
{
    @NotNull
    @Length(min = 7, max = 7, message = "cmdCode长度应是xxx.nnn格式！")
    private String cmdCode;

    private Map<String, Object> reqData;
}
````

* `defineName = "${cmdCode}"`：Retrieve the value of the 'cmdCode' property from the WebReq object as the value of 'defineName'.
* `targetName = "reqData"`: Validate the `reqData` property of the WebReq object. There is no need to add constraints to the `reqData` property.

#### 2.1.2 XML File Mode

````
<constraint-mappings
        xmlns="https://jakarta.ee/xml/ns/validation/mapping"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://jakarta.ee/xml/ns/validation/mapping
            https://jakarta.ee/xml/ns/validation/validation-mapping-3.0.xsd"
        version="3.0">

    <bean class="io.github.sham2k.test.bean.PageQry2" ignore-annotations="false">
        <class ignore-annotations="false">
            <constraint annotation="io.github.sham2k.validation.constraints.ValueMap">
                <element name="targetName">
                    <value>reqData</value>
                </element>
                <element name="defineName">${cmdCode}</element>
            </constraint>
        </class>
        <field name="cmdCode">
            <constraint annotation="org.hibernate.validator.constraints.Length">
                <message>cmdCode长度应是xxx.nnn格式！</message>
                <element name="max">7</element>
                <element name="min">7</element>
            </constraint>
        </field>
    </bean>
</constraint-mappings>
````

* Add a `ValueMap` constraint to `<bean>/<class>` and set the attribute through the 'targetName' element, and set the constraint set name through '${cmdCode}'.
* In this scenario, it is not necessary to add constraints to the 'reqData' attribute.

### 2.2 Validate MAP Instance

Refer to the following code and directly use the constraint set to validate the Map instance or other object. The objects validated using this pattern do not require constraints defined by HibernateValidation.

````
Map<String, Object> value = new HashMap<>();
...

Set<ConstraintViolation<Map<String, Object>>> result = ValidatorManager.validate(value, "cmd.001");
if (result.isEmpty()) {
    System.out.println("**** PASS");
} else {
    System.out.println("**** FAIL");
    result.forEach(System.out::println);
}
````

### 2.3 Validate Nested Map

If the element value of the MAP instance which be validated is a Map instance, refer to the following configuration to validate the nested Map instance.

````
<constraint-mappings>
    <bean class="cmd.005" ignore-annotations="false">
        ...
        <field name="user">
            <valid/>
        </field>
        <field name="subMap">
            <valid name="sub.001" />
        </field>
    </bean>
</constraint-mappings>

````

* Add a '<valid>' constraint to the nested MAP field, indicating that the field needs to be validated, and indicate the name of the constraint set through the 'name' attribute.

# 3 Change History

## 3.1 Version 0.1.0
### 3.1.1 Append
* Implement all basic functions

## 3.2 Version 0.2.0
### 3.2.1 Append
* Support validate nested MAP instances.

## 3.3 Version 0.2.1
### 3.3.1 Modify
* Migrate `ValidatorManager` from package `io.github.sham2k.validation.config` to `io.github.sham2k.validation.validator`.

## 3.4 Version 0.2.2
### 3.4.1 Append
* Implement direct validation of objects using constraint sets.

## 3.5 Version 0.2.3
### 3.5.1 Modify
* Refactoring verification method, supporting setting the 'targetName' parameter.

## 3.6 Version 0.2.4
### 3.6.1 Modify
* Fix path name error when validate nested maps.
