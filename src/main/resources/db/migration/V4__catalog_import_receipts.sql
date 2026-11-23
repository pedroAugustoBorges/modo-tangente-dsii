CREATE TABLE public.catalog_imports (
    source VARCHAR(500) PRIMARY KEY,
    sha256 VARCHAR(64) NOT NULL
);
