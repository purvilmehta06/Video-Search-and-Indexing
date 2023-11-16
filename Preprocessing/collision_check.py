import os

set_of_rgb_values = set()
rgb_values_dic = {}

def add_rgb_values_to_set(file_name):
	values = []
	with open(file_name, 'r') as file:
		for line in file:
			values.append(line)
	list_values = []
	for value in values:
		list_values.append(tuple([int(num) for num in value.split(',')]))
	six_hundred_consecutive_values = []
	count = 0
	k = 0
	for value in list_values:
		six_hundred_consecutive_values.append(value)
		k+=1
		if (len(six_hundred_consecutive_values) == 600):
			# if tuple(six_hundred_consecutive_values) in set_of_rgb_values:
			# 	# print("Already present in set")
			# 	count+=1
			# else:
			rgb_values_dic[tuple(six_hundred_consecutive_values)] = [file_name, k]
			set_of_rgb_values.add(tuple(six_hundred_consecutive_values))
			six_hundred_consecutive_values.pop(0)
	print(count)

def check_for_values(file_name):
	values = []
	with open(file_name, 'r') as file:
		for line in file:
			values.append(line)
	list_values = []
	for value in values:
		list_values.append(tuple([int(num) for num in value.split(',')]))
	if tuple(list_values) in set_of_rgb_values:
		print(rgb_values_dic[tuple(list_values)])
	else:
		print("SED!")

folder_path = "rgb_values/"
for value_file in os.listdir(folder_path):
	add_rgb_values_to_set(folder_path + value_file)

folder_path = "query_rgb_values/"
for value_file in os.listdir(folder_path):
	check_for_values(folder_path + value_file)

