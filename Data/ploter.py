import matplotlib.pyplot as plt
import os
from os.path import join


def read_file(filename):
    value = []
    client = []
    full = []
    sort = []
    f = open(filename, "r")
    head = True
    for line in f:
        if head:
            head = False
            continue
        arr = line.split(",")
        value.append(float(arr[0]))
        client.append(float(arr[1]))
        full.append(float(arr[2]))
        sort.append(float(arr[3]))
    return value, client, full, sort


def make_plot(val, tb, pb, nb, name, metric):
    plt.clf()
    tb_line, = plt.plot(val, tb, label="Blocking One Thread")
    pb_line, = plt.plot(val, pb, label="Blocking Pool")
    nb_line, = plt.plot(val, nb, label="Non-Blocking")
    plt.legend(handles=(tb_line, pb_line, nb_line))
    plt.xlabel(name)
    plt.ylabel("time, ms")
    plt.title(metric + " time")
    plt.savefig(name + "_" + metric + ".png")
    # plt.show()


def plot_by_value(value_name):
    value, tb_client, tb_full, tb_sort = read_file(join(value_name, "TB_" + value_name + ".txt"))
    _, pb_client, pb_full, pb_sort = read_file(join(value_name, "PB_" + value_name + ".txt"))
    _, nb_client, nb_full, nb_sort = read_file(join(value_name, "NB_" + value_name + ".txt"))
    make_plot(value, tb_sort, pb_sort, nb_sort, value_name, "sort")
    make_plot(value, tb_full, pb_full, nb_full, value_name, "full")
    make_plot(value, tb_client, pb_client, nb_client, value_name, "client")


if __name__ == '__main__':
    plot_by_value("D")
