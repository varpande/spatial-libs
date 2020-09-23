wget -m http://spatial-libs.db.in.tum.de/

echo "downloaded all datasets .."
echo "creating resources directory .."
mv spatial-libs.db.in.tum.de resources/
find . -type f -name 'index.html' -delete

MD5_RESULT=`find resources -type f -exec md5sum {} \; | sort | md5sum | awk '{ print $1 }'`
if [ "${MD5_RESULT}" != "755a42e6688a2ff3bd59a5edf827d6ff" ]; then
  echo "Checksum of download did not match. Please try running the ./setup.sh again."
  exit
fi

echo "creating symlinks .."
curr_dir=$(echo $PWD)
parentdir=`eval "cd $curr_dir/..;pwd;cd - > /dev/null"`

ln -s $curr_dir/resources/ $parentdir/java/
echo "created symlink from $curr_dir/resources/ to $parentdir/java/"
ln -s $curr_dir/resources/ $parentdir/cpp/
echo "created symlink from $curr_dir/resources/ to $parentdir/cpp/"

echo "begin creating binary files for the points datasets .."


################## Convert projected points dataset files to binary in java #####################

echo "creating binary files in java for projected datasets .."
mkdir $curr_dir/resources/datasets/projected/binary/
mkdir $curr_dir/resources/datasets/projected/binary/java/
cd $curr_dir/converters/
javac PointsToBinary.java

BAR='#########'
progress=1
echo -ne "\rProgress: ${BAR:0:0} (0%)"
for i in 10 15 20 25 30 35 40 45 50
do
	in_file=$curr_dir/resources/datasets/projected/"$i"M_rides.csv
	out_file=$curr_dir/resources/datasets/projected/binary/java/"$i"M_rides.bin
	java PointsToBinary $in_file $out_file
	progress_percent=$((($progress * 100)/9))
	progress=$(($progress + 1))
	for i in {1..9}; do
		echo -ne "\rProgress: ${BAR:0:$progress} ($progress_percent%)"
	done
done
echo -ne '\n'

################## Convert projected points dataset files to binary in cpp #####################
echo "creating binary files in cpp for projected datasets .."
mkdir $curr_dir/resources/datasets/projected/binary/cpp/
g++ points_to_binary.cpp -std=c++14 -g0 -O3 -o points_to_binary

progress=1
for i in 10 15 20 25 30 35 40 45 50
do
	in_file=$curr_dir/resources/datasets/projected/"$i"M_rides.csv
	out_file=$curr_dir/resources/datasets/projected/binary/cpp/"$i"M_rides.bin
	cat $in_file | ./points_to_binary $out_file
	progress_percent=$((($progress * 100)/9))
	progress=$(($progress + 1))
	for i in {1..9}; do
		echo -ne "\rProgress: ${BAR:0:$progress} ($progress_percent%)"
	done
done
echo -ne '\n'

echo "creating binary files in cpp for raw gps datasets .."
mkdir $curr_dir/resources/datasets/raw/binary/
mkdir $curr_dir/resources/datasets/raw/binary/cpp/
g++ points_to_binary.cpp -std=c++14 -g0 -O3 -o points_to_binary

echo -ne "\rProgress: ${BAR:0:0} (0%)"
progress=1
for i in 10 15 20 25 30 35 40 45 50
do
	in_file=$curr_dir/resources/datasets/raw/"$i"M_rides.csv
	out_file=$curr_dir/resources/datasets/raw/binary/cpp/"$i"M_rides.bin
	cat $in_file | ./points_to_binary $out_file
	progress_percent=$((($progress * 100)/9))
	progress=$(($progress + 1))
	for i in {1..9}; do
		echo -ne "\rProgress: ${BAR:0:$progress} ($progress_percent%)"
	done
done
echo -ne '\n'

echo -e "\n\nSetup is complete. Please go to individual folders java and cpp to build and run the experiments."
