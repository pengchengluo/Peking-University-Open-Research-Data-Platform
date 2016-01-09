echo "Delete elasticsearch index: dataverse"
curl -XDELETE 'http://localhost:9200/dataverse/'

echo ""
echo "Create elasticsearch index: dataverse"
curl -XPUT 'http://localhost:9200/dataverse/' -d @elasticsearch-statistics-mapping.json

echo ""
echo "Index usage ..."
python3 dvn_usage_log_index.py $1 $2 $3
