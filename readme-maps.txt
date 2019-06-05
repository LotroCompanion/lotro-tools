1) Clear lotro-tools/data/maps
2) Run MainDynMapLoader
3) Run MarkersMerge

4) Use Beyond Compare to report changes from the new maps (lotro-tools/data/maps/output) to the reference maps (lotro-maps-db):
a) Links
- ignore links diffs (ref contains usefull links, while the new maps do not)
b) Maps/markers
- copy new maps to reference
- be careful that "northern barrowdowns" and "caras galadhon" maps are missing in dynmap
- be careful for map renames (e.g huose of beorn in Update 24)
- copy categories.xml

5) Run MainMapsCleaner
This shall remove unused categories (4)

6) Run MainMapsChecker
This will check for consistency errors in the reference maps.
- we should have no links errors
- duplicate IDs errors do exist. Check for new errors.

7) Run MainLinkEditor to add new links between maps

8) Commit result
