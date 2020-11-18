    sudo podman stop spark_local_box_1
    sudo podman rm spark_local_box_1
    sudo podman rmi localhost/spark_local_box
    sudo podman build -f Dockerfile -t localhost/spark_local_box -m 2g --cpuset-cpus 1,3,5 --cpu-shares 512
    sudo podman run -d --network host -v shara:/shara --name spark_local_box_1 -m 2g --cpuset-cpus 1,3,5 --cpu-shares 512 spark_local_box # example; set desired values

detect container "shara" dir

    sudo podman volume inspect shara

edit spark_local_box.conf in the container "shara" dir or copy new spark_local_box.conf to the container "shara" dir
 
    sudo cp ./shara/spark_local_box.conf /var/lib/containers/storage/volumes/shara/_data/spark_local_box.conf
    
or

    sudo nano /var/lib/containers/storage/volumes/shara/_data/spark_local_box.conf
    
or etc.

start gateway

    sudo podman exec -d -t spark_local_box_1 java -jar spark_local_box-1.0-uber.jar com.sergei_baranov.spark_local_box.MsApp # ENTRYPOINT
    wget -qO- http://localhost:9077/ping # 9077 see spark_local_box.conf
 
copy jobbers jars to the container "shara" dir

    sudo cp /some/path/cbc_liquidity_datain-1.0-SNAPSHOT-uber.jar /var/lib/containers/storage/volumes/shara/_data/cbc_liquidity_datain-1.0-SNAPSHOT-uber.jar
    
    sudo cp /some/path/cbc_liquidity_calc-1.0-SNAPSHOT-uber.jar /var/lib/containers/storage/volumes/shara/_data/cbc_liquidity_calc-1.0-SNAPSHOT-uber.jar
    
run jobs (with spark_local_box.conf api)

    wget -qO- http://localhost:9077/jobs/cbc_liquidity/datain/2020-11-01/[...]
    
    wget -qO- http://localhost:9077/jobs/cbc_liquidity/calc/2020-11-01/[...]
    
run jobs by exec (set all params explicitly, spark_local_box.conf will have no effect) for debug or any purposes

    sudo podman exec -t spark_local_box_1 java -jar /shara/cbc_liquidity_datain-1.0-SNAPSHOT-uber.jar com.sergei_baranov.cbonds_calc_liquidity.cbc_liquidity_datain.DatainApp "2020-11-17" "boo" "moo" "secret"

    sudo podman exec -t spark_local_box_1 /usr/local/spark/bin/spark-submit --conf spark.ui.enabled=false --class com.sergei_baranov.cbonds_calc_liquidity.cbc_liquidity_calc.CalcApp --master local[2] local:/shara/cbc_liquidity_calc-1.0-SNAPSHOT-uber.jar "2020-11-17" "localhost:3306" "user" "password"
    
    sudo podman exec -t spark_local_box_1 /usr/local/spark/bin/spark-submit --conf spark.ui.enabled=false|true --class ... --master local[2] local:/shara/....jar param param param

    