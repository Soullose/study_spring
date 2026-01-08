### api（接口契约层）
* 对应 DDD 层：接口层（Interface Layer）中的契约部分  
* 职责：定义系统对外暴露的接口契约与数据传输对象（DTO）。不包含任何实现代码。包含：  
  * 服务接口（如 `UserService`）  
  * 请求/响应 DTO（如 `CreateUserRequest`、`UserResponse`）  
* 依赖关系：零依赖（仅声明）。`app` 实现这些接口，`adapter` 调用这些接口，形成“契约‑实现‑调用”三角关系。  