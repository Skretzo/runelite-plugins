# Better Tile Rendering Example

## Info
Rendering multiple tiles as a semi-continuous `GeneralPath` line with `moveTo` and `lineTo`
seems to be more efficient than using [renderPolygon in OverlayUtil](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/overlay/OverlayUtil.java#L61) to draw individual `Shape` polygons.

## Known issues
- Stops rendering with low camera angle
