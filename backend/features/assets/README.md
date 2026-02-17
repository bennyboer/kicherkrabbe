# Assets Module

Manages uploaded assets (images, files) used across the application.

## Asset Reference Tracking

The assets module tracks which resources (fabrics, patterns, products, highlights) reference each asset.
This enables identifying orphaned assets that are no longer used by any resource.

### How It Works

When a resource that references assets is created, updated, or deleted, the originating module publishes
domain events. The assets module listens to these events and maintains a reference table
(`assets_references`) mapping each asset to the resources that use it.

### Adding a New Module

When a new module references assets, the following steps are required:

1. Ensure the module publishes `CREATED`, image update, and `DELETED` events containing asset IDs.
2. Add corresponding record types for typed deserialization in `AssetsMessaging.java`.
3. Add three listener beans in `AssetsMessaging.java`:
   - `assets_on<Module>CreatedUpdateAssetReferences` — calls `updateAssetReferences`
   - `assets_on<Module>ImageUpdatedUpdateAssetReferences` — calls `updateAssetReferences`
   - `assets_on<Module>DeletedRemoveAssetReferences` — calls `removeAssetReferencesByResource`
4. Add the new resource type to `AssetReferenceResourceType` enum.
5. Add corresponding test cases in `AssetsMessagingTest.java`.
6. Create a migration script to backfill existing references from the module's lookup collection.
