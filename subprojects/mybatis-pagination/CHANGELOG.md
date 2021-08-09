# Changelog

## 0.1.3 (202x-xx-xx)

### Modification

- 🚚 Rename: resolver from `PageRequestResolverAdaptor` to `PageRequestResolver`

### New features

- ✨ Add: default constructor of `PageRequestResolver`

### Dependencies

- ⬆️ Upgrade: dependency `byte-buddy` from `1.11.8` to `1.11.12`



## 0.1.2 (2021-07-24)

### Dependencies

- ⬆️ Upgrade: dependency `byte-buddy` from `1.11.1` to `1.11.8`
- ⬆️ Upgrade: dependency `spring-framework` from `5.3.7` to `5.3.9`

### Troubleshooting

- 🐞 Fix: parameter mapping miss when there are additional `ParameterMapping` and non-additional `ParameterMapping` in one query at the same time.



## 0.1.1 (2021-05-20)

### Modification

- ♻️ Change: dependency scope for integration with spring boot
- ⚡️ Make: `Paginator.items` unmodifiable

### New features

- ✨ Add: adaptor for `HandlerMethodArgumentResolver` of `PageRequest`



## 0.1.0 (2021-05-19)

- 🎉 Begin: project

