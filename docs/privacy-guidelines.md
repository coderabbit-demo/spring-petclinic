# Privacy and data-handling guidelines

These rules apply to all code in this repository. CodeRabbit reviews every pull request against them.

## Personal data
- Owner contact details (first name, last name, address, city, telephone) are personal data.
- Never log personal data. Log entity IDs only (for example `ownerId=42`).
- API responses must return only the fields the caller needs. Do not expose whole entities by default.

## Access control
- Any endpoint that returns owner, pet or visit data must check that the caller is allowed to see it.
- Never trust an ID from the request alone to decide access.

## Medical records
- Visit history is a medical record. Past visits must not be edited or deleted.
- Corrections are made by adding a new visit that references the original.

## Data access
- Use Spring Data query methods or parameterized queries only. Never build SQL or JPQL by string concatenation.
- List and search endpoints must be paginated.
