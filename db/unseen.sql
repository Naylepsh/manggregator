select asset.name, chapter.no, chapter.url
from asset
join chapter on chapter.assetId = asset.id
where chapter.seen = 0;
