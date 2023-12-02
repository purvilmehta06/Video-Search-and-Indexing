import os

set_of_consecutive_rgb_sum_values = set()

# Function to check for consecutive RGB sum values for 15 seconds
def add_rgb_values_to_set(file_name):
	values = []
	with open(file_name, 'r') as file:
		for line in file:
			values.append(line)
	list_values = []
	for value in values:
		list_values.append(tuple([int(num) for num in value.split(' ')]))
	consecutive_values = []
	for value in list_values:
		consecutive_values.append(value)
		if (len(consecutive_values) == 300):
			if tuple(consecutive_values) in set_of_consecutive_rgb_sum_values:
				print("Already present in set")
			else:
			    set_of_consecutive_rgb_sum_values.add(tuple(consecutive_values))
			    consecutive_values.pop(0)

if __name__ == "__main__":
    folder_path = "rgb_sum_values/"
    for value_file in os.listdir(folder_path):
        print("Processing: ", value_file)
        add_rgb_values_to_set(folder_path + value_file)
