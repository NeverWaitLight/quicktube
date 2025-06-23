# Docker镜像加速配置

如果遇到Docker镜像拉取失败的问题可能是网络连接问题 可以通过配置镜像加速器来解决

## Windows系统配置方法

1. 右键点击桌面右下角Docker图标
2. 选择"Settings"
3. 点击左侧"Docker Engine"
4. 在配置JSON中添加或修改registry-mirrors字段
5. 添加国内加速器地址 例如

```json
{
  "registry-mirrors": [
    "https://registry.docker-cn.com",
    "https://mirror.baidubce.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "builder": {
    "gc": {
      "enabled": true,
      "defaultKeepStorage": "20GB"
    }
  }
}
```

6. 点击"Apply & Restart"按钮重启Docker服务

## Linux系统配置方法

编辑`/etc/docker/daemon.json`文件(如果不存在则创建):

```json
{
  "registry-mirrors": [
    "https://registry.docker-cn.com",
    "https://mirror.baidubce.com",
    "https://docker.mirrors.ustc.edu.cn" 
  ]
}
```

重启Docker服务:

```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 检查配置是否生效

```bash
docker info | grep Mirror
```

如果配置成功会显示配置的镜像加速器地址 