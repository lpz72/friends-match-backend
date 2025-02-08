package org.lpz.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {

    private Long id;
}
