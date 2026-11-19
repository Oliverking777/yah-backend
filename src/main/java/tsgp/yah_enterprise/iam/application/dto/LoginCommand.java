package tsgp.yah_enterprise.iam.application.dto;

import tsgp.yah_enterprise.iam.domain.model.ClientType;

/**
 * Input for the LoginUseCase.
 * ipAddress and userAgent are only recorded for auditing and may be null.
 */
public record LoginCommand(
        String userName,
        String password,
        ClientType clientType,
        String ipAddress,
        String userAgent
) {
    @Override
    public String toString() {
        return "LoginCommand[userName=" + userName + ", password=protected, clientType=" + clientType + "]";
    }
}