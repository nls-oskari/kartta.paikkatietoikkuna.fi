UPDATE public.portti_view SET application='geoportal' WHERE application='full-map'; 
UPDATE public.portti_view SET application='geoportal-3D' WHERE application='full-map-3D';
UPDATE public.portti_view SET application='embedded' WHERE application='published-map_ol3'; 
UPDATE public.portti_view SET application='embedded-3D' WHERE application='published-map-3D';
