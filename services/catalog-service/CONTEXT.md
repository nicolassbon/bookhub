# Catalog

This context defines canonical book metadata and discovery in BookHub. It exists to own what a book is, where its metadata comes from, and how that metadata is exposed to the rest of the platform.

## Language

**Book**:
The canonical book owned by Catalog and exposed for discovery and downstream reference.
_Avoid_: Catalog Book, library book, user book

**Provider**:
The external source from which Catalog imports or enriches book metadata.
_Avoid_: External Book Source, Catalog Source

**Normalization**:
The process of transforming Provider metadata into Catalog's canonical Book model.
_Avoid_: Import (when the canonical transformation is meant), Ingestion

**Author**:
The supporting metadata concept that describes who wrote a Book, without becoming the primary focus of the Catalog context in V1.
_Avoid_: primary catalog aggregate, book owner

## Relationships

- A **Book** is the canonical metadata source referenced by downstream contexts.
- A **Provider** supplies metadata that Catalog may normalize into a **Book**.
- **Normalization** turns Provider data into Catalog-owned canonical metadata.
- An **Author** may be referenced by one or more **Book** records.

## Example dialogue

> **Dev:** "When Library stores a title and cover, does that make Library the owner of the book?"
> **Domain expert:** "No — Catalog still owns the **Book**. Library only keeps a downstream snapshot for its own workflows."

## Flagged ambiguities

- `Catalog Book` is unnecessary inside the Catalog context; use **Book** here and reserve cross-context qualification for downstream contexts like Library.
- `External Book Source` is too verbose for the current Catalog language; use **Provider** for the metadata origin.
- `Import` and `Ingestion` are not the best terms when the key idea is canonical transformation; use **Normalization** for that domain step.
- `Author` matters in Catalog, but it is secondary to the core Book-discovery and normalization language in V1.
