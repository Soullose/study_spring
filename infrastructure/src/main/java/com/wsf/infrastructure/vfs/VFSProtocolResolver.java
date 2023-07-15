package com.wsf.infrastructure.vfs;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.VfsResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class VFSProtocolResolver implements ProtocolResolver {
	@Override public Resource resolve(@NonNull String location, @NonNull ResourceLoader resourceLoader) {
		return StringUtils.startsWith(location, VFSConstant.VFS_PROTOCOL) ? new VfsResource(location) : null;
	}
}
