UPDATE portti_view
SET
  creator = -1,
  is_public = true
WHERE creator = 10110
AND type = 'DEFAULT';
