# Changelog

## 0.1.3 (2021-06-02)

### New features

- ✨ Add: `create(Class, Function, Function, ClassLoader)` in `TypeHandlerFactory`
- ✨ Add: method `TypeHandlers#builder(ClassLoader)`
- ✨ Add: constructors `this(Class, ClassLoader)`, `this(String, ClassLoader)`, `this(Reflections, ClassLoader)` in `DynamicCodeEnumTypeHandlerGenerator`

### Dependencies

- ⬆️ Upgrade: dependency `Byte Buddy` from `1.11.0` to `1.11.1`

### Troubleshooting

- 🐞 Fix: problem that did not guarantee class uniqueness by class loader in a project that uses `spring-boot-devtools`; In that project, `Thread#getContextClassLoader()` is `org.springframework.boot.devtools.restart.classloader.RestartClassLoader`. It does not comply with [the class loading principles](https://dzone.com/articles/demystify-java-class-loading). Therefore, even the same class may have different memory addresses.

> Class loading is guided by three simple principles:
>
> 1. Delegation – The delegation principle guides us when class is not already loaded. In such a case, the child class loader asks its parent to load the class until bootstrap class loader is checked (*It ensures same parent class on top of object hierarchy java.lang.Object*)
> 2. Visibility – The visibility principle ensures that the child class loader can see all the classes loaded by its parent. But, the inverse of that is not true, the parent can’t see the class loaded by its child (*otherwise it’s like a flat class loader, loading everything without any isolation level*)
> 3. Uniqueness – The uniqueness principle ensure that a class will be loaded exactly once in the lifetime of a class loader hierarchy (*since the child has visibility to parent class, it never tries to load classes already loaded by the parent, but two siblings may end up loading the same class in their respective class loaders*)



## 0.1.2 (2021-05-20)

### Modification

- ♻️ Change: dependency scope for integration with spring boot



## 0.1.1 (2021-05-20)

### Modification

- 🚚 Rename: class `DynamicCodeEnumTypeHandlerFinder` to `DynamicCodeEnumTypeHandlerGenerator`
- 🚚 Rename: `TypeHandlerSupport` => `TypeHandlerFactory`
- ♻️ Replace: `DynamicCodeEnumTypeHandlerFinder#findTypeHandlers()` with `DynamicCodeEnumTypeHandlerGenerator#generateAll()`
- 🔊 Change: validation log

### New features

- ✨ Add: methods `generate(Policy, Class...)` in `DynamicCodeEnumTypeHandlerGenerator`

### Troubleshooting

- 🐞 Fix: wrong exclusion of all subclasses of `CodeEnum` at `DynamicCodeEnumTypeHandlerGenerator#generateAll()`



## 0.1.0 (2021-05-19)

- 🎉 Begin: project

