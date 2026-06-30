CREATE TABLE ps_folders (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255)  NOT NULL,
    parent_id       BIGINT        NULL,
    path            VARCHAR(2048) NOT NULL,
    created_by      BIGINT        NOT NULL,
    created_at      DATETIME      NOT NULL,
    deleted_at      DATETIME      NULL,
    deleted_by      BIGINT        NULL,
    delete_batch_id CHAR(36)      NULL,
    purge_at        DATETIME      NULL,
    purged_at       DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_psf_parent  FOREIGN KEY (parent_id)  REFERENCES ps_folders(id),
    CONSTRAINT fk_psf_creator FOREIGN KEY (created_by) REFERENCES employees(id),
    CONSTRAINT fk_psf_deleter FOREIGN KEY (deleted_by) REFERENCES employees(id),
    INDEX idx_psf_parent (parent_id),
    INDEX idx_psf_path   (path(500)),
    INDEX idx_psf_trash  (deleted_at, purge_at)
);

CREATE TABLE ps_files (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    folder_id       BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_by      BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL,
    deleted_at      DATETIME     NULL,
    deleted_by      BIGINT       NULL,
    delete_batch_id CHAR(36)     NULL,
    purge_at        DATETIME     NULL,
    purged_at       DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_psfi_folder  FOREIGN KEY (folder_id)  REFERENCES ps_folders(id),
    CONSTRAINT fk_psfi_creator FOREIGN KEY (created_by) REFERENCES employees(id),
    CONSTRAINT fk_psfi_deleter FOREIGN KEY (deleted_by) REFERENCES employees(id),
    INDEX idx_psfi_folder (folder_id),
    INDEX idx_psfi_trash  (deleted_at, purge_at)
);

CREATE TABLE ps_file_versions (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    file_id        BIGINT        NOT NULL,
    version_number INT           NOT NULL,
    storage_key    VARCHAR(1024) NOT NULL,
    file_size      BIGINT        NOT NULL,
    is_current     BOOLEAN       NOT NULL DEFAULT FALSE,
    uploaded_by    BIGINT        NOT NULL,
    uploaded_at    DATETIME      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_psv_file_version (file_id, version_number),
    CONSTRAINT fk_psv_file     FOREIGN KEY (file_id)     REFERENCES ps_files(id),
    CONSTRAINT fk_psv_uploader FOREIGN KEY (uploaded_by) REFERENCES employees(id),
    INDEX idx_psv_current (file_id, is_current)
);

CREATE TABLE ps_permissions (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    employee_id BIGINT   NOT NULL,
    granted_by  BIGINT   NOT NULL,
    granted_at  DATETIME NOT NULL,
    revoked_at  DATETIME NULL,
    revoked_by  BIGINT   NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_psp_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_psp_granter  FOREIGN KEY (granted_by)  REFERENCES employees(id),
    CONSTRAINT fk_psp_revoker  FOREIGN KEY (revoked_by)  REFERENCES employees(id),
    INDEX idx_psp_employee (employee_id, revoked_at)
);

CREATE TABLE ps_settings (
    `key`      VARCHAR(100) NOT NULL,
    value      VARCHAR(500) NOT NULL,
    updated_by BIGINT       NOT NULL,
    updated_at DATETIME     NOT NULL,
    PRIMARY KEY (`key`),
    CONSTRAINT fk_pss_updater FOREIGN KEY (updated_by) REFERENCES employees(id)
);

INSERT INTO ps_settings (`key`, value, updated_by, updated_at)
SELECT 'trash.retention_days', '30', id, NOW()
FROM employees WHERE role = 'ADMIN' LIMIT 1;
