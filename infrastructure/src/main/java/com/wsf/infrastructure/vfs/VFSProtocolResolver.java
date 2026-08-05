package com.wsf.infrastructure.vfs;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.VfsResource;
import org.springframework.lang.NonNull;

//@Component
public class VFSProtocolResolver implements ProtocolResolver {
	// private static final Logger log = LoggerFactory.getLogger(VFSProtocolResolver.class);

	public VFSProtocolResolver() {
	}

	@Override
	public Resource resolve(@NonNull String location, @NonNull ResourceLoader resourceLoader) {
		// log.debug("添加{}解析策略", VFSConstant.VFS_PROTOCOL);
		return StringUtils.startsWith(location, VFSConstant.VFS_PROTOCOL) ? new VfsResource(location) : null;
	}
}
