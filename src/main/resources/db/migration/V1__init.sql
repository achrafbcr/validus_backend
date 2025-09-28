CREATE TABLE companies (
    code NVARCHAR(10) NOT NULL PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    iban NVARCHAR(34),
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE suppliers (
    id NVARCHAR(36) NOT NULL PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    tax_id NVARCHAR(50),
    rib NVARCHAR(80),
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE invoices (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    number NVARCHAR(50) NOT NULL,
    supplier_id NVARCHAR(36) NOT NULL,
    company_code NVARCHAR(10) NOT NULL,
    po_number NVARCHAR(50),
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    currency CHAR(3) NOT NULL,
    amount_ht DECIMAL(19,2) NOT NULL,
    amount_tva DECIMAL(19,2) NOT NULL,
    amount_ttc DECIMAL(19,2) NOT NULL,
    status NVARCHAR(30) NOT NULL,
    workflow_id NVARCHAR(36),
    yardi_txn_id NVARCHAR(50),
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2,
    hash_content NVARCHAR(128),
    flags NVARCHAR(MAX),
    has_paper_original BIT NOT NULL DEFAULT 0,
    invoice_year INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_invoice_supplier_number_year ON invoices (supplier_id, number, invoice_year);
CREATE INDEX idx_invoice_status ON invoices (status);
CREATE INDEX idx_invoice_company ON invoices (company_code);
CREATE INDEX idx_invoice_po ON invoices (po_number);
CREATE INDEX idx_invoice_created_at ON invoices (created_at);

CREATE TABLE invoice_lines (
    id BIGINT IDENTITY PRIMARY KEY,
    invoice_id UNIQUEIDENTIFIER NOT NULL,
    designation NVARCHAR(255) NOT NULL,
    quantity DECIMAL(19,2) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    amount_line DECIMAL(19,2) NOT NULL,
    cost_center NVARCHAR(50),
    gl_account NVARCHAR(50),
    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE TABLE attachments (
    id BIGINT IDENTITY PRIMARY KEY,
    invoice_id UNIQUEIDENTIFIER NOT NULL,
    type NVARCHAR(30) NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    mime NVARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    sha256 NVARCHAR(64),
    storage_ref NVARCHAR(512) NOT NULL,
    CONSTRAINT fk_attachments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE TABLE audit_logs (
    id BIGINT IDENTITY PRIMARY KEY,
    invoice_id UNIQUEIDENTIFIER,
    action NVARCHAR(60) NOT NULL,
    by_user NVARCHAR(100) NOT NULL,
    at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    meta NVARCHAR(MAX)
);
CREATE INDEX idx_audit_invoice ON audit_logs (invoice_id);

CREATE TABLE payment_batches (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    status NVARCHAR(30) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    company_code NVARCHAR(10) NOT NULL,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2
);

CREATE TABLE payment_batch_items (
    id BIGINT IDENTITY PRIMARY KEY,
    batch_id UNIQUEIDENTIFIER NOT NULL,
    invoice_id UNIQUEIDENTIFIER NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    supplier_id NVARCHAR(36) NOT NULL,
    CONSTRAINT fk_payment_items_batch FOREIGN KEY (batch_id) REFERENCES payment_batches(id),
    CONSTRAINT fk_payment_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);
