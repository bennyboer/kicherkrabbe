db.getCollection('fabrics_events').updateMany(
  { 'eventName': 'IMAGE_UPDATED' },
  {
    $set: {
      'eventName': 'IMAGES_UPDATED',
      'payload.exampleImages': []
    }
  }
)

db.getCollection('fabrics_lookup').updateMany(
  { 'exampleImageIds': { $exists: false } },
  {
    $set: {
      'exampleImageIds': []
    }
  }
)
