# Changelog

## 0.2.0 (2021-07-10)

### Modification

- ð¿ Clean up: constructors of `DynamicCodeEnumTypeHandlerGenerator`
- â¡ï¸ Improve: performance of `CodeEnumTypeHandler` using cache
- ð Update: javadoc

### New features

- â¨ Add: `CodeEnumConverterFactory` for conversion of `CodeEnum` from `String`

### Dependencies

- â¬ï¸ Upgrade: dependency `byte-buddy` from `1.11.1` to `1.11.6`
- â¬ï¸ Upgrade: dependency `springframework` from `5.3.7` to `5.3.8`

### Troubleshooting

- ð Fix: wrong return type for generation of dynamic type handler in `TypeHandlerFactory`



## 0.1.3 (2021-06-02)

### New features

- â¨ Add: `create(Class, Function, Function, ClassLoader)` in `TypeHandlerFactory`
- â¨ Add: method `TypeHandlers#builder(ClassLoader)`
- â¨ Add: constructors `this(Class, ClassLoader)`, `this(String, ClassLoader)`, `this(Reflections, ClassLoader)` in `DynamicCodeEnumTypeHandlerGenerator`

### Dependencies

- â¬ï¸ Upgrade: dependency `Byte Buddy` from `1.11.0` to `1.11.1`

### Troubleshooting

- ð Fix: problem that did not guarantee class uniqueness by class loader in a project that uses `spring-boot-devtools`; In that project, `Thread#getContextClassLoader()` is `org.springframework.boot.devtools.restart.classloader.RestartClassLoader`. It does not comply with [the class loading principles](https://dzone.com/articles/demystify-java-class-loading). Therefore, even the same class may have different memory addresses.

> Class loading is guided by three simple principles:
>
> 1. Delegation â The delegation principle guides us when class is not already loaded. In such a case, the child class loader asks its parent to load the class until bootstrap class loader is checked (*It ensures same parent class on top of object hierarchy java.lang.Object*)
> 2. Visibility â The visibility principle ensures that the child class loader can see all the classes loaded by its parent. But, the inverse of that is not true, the parent canât see the class loaded by its child (*otherwise itâs like a flat class loader, loading everything without any isolation level*)
> 3. Uniqueness â The uniqueness principle ensure that a class will be loaded exactly once in the lifetime of a class loader hierarchy (*since the child has visibility to parent class, it never tries to load classes already loaded by the parent, but two siblings may end up loading the same class in their respective class loaders*)



## 0.1.2 (2021-05-20)

### Modification

- â»ï¸ Change: dependency scope for integration with spring boot



## 0.1.1 (2021-05-20)

### Modification

- ð Rename: class `DynamicCodeEnumTypeHandlerFinder` to `DynamicCodeEnumTypeHandlerGenerator`
- ð Rename: `TypeHandlerSupport` => `TypeHandlerFactory`
- â»ï¸ Replace: `DynamicCodeEnumTypeHandlerFinder#findTypeHandlers()` with `DynamicCodeEnumTypeHandlerGenerator#generateAll()`
- ð Change: validation log

### New features

- â¨ Add: methods `generate(Policy, Class...)` in `DynamicCodeEnumTypeHandlerGenerator`

### Troubleshooting

- ð Fix: wrong exclusion of all subclasses of `CodeEnum` at `DynamicCodeEnumTypeHandlerGenerator#generateAll()`



## 0.1.0 (2021-05-19)

- ð Begin: project

