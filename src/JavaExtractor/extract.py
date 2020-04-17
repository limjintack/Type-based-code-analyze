#!/usr/bin/python

import itertools
import multiprocessing
import os
import sys
import shutil
import subprocess
from threading import Timer
import sys
from argparse import ArgumentParser
from subprocess import Popen, PIPE, STDOUT, call

import select
import time

#import parmap 
#from multiprocessing import Manager

def temp_get_immediate_subdirectories(args, a_dir):
    
    command = ['java', '-cp', args.jar, 'JavaExtractor.App',
               '--max_path_length', str(args.max_path_length), '--max_path_width', str(args.max_path_width),
               '--dir', a_dir, '--num_threads', str(args.num_threads)]
    fd_popen = subprocess.Popen(command, stdout=subprocess.PIPE)
    
    while True:
        line = fd_popen.stdout.readline()
        if not line:
            break
        try:
            print(line.rstrip().decode("utf-8"))
        except:
            continue
    #for line in fd_popen.stdout:
    #    print (line.rstrip().decode("utf-8"))
    
    #ast = fd_popen.read().strip().decode("utf-8") 
    #print(ast)
    '''
    y=select.poll() 
    y.register(x.stdout,select.POLLIN) 
    
    while True: 
        if y.poll(1): 
            print (x.stdout.readline())
        else: 
            time.sleep(1)
    
    fd_popen.close()
    
    
    for name in os.listdir(a_dir):
        if os.path.isdir(os.path.join(a_dir, name)):
            temp_get_immediate_subdirectories(args, os.path.join(a_dir, name))
        else:
            file_path = a_dir + "/" + name
            make_ast(args, file_path)
    '''
    
def make_ast(args, file_path):
    args.file = file_path
    
    if not os.path.isdir("Code_to_AST/" + os.path.split(file_path)[0]):
        os.makedirs("Code_to_AST/" + os.path.split(file_path)[0])
        
    if os.path.splitext(file_path)[1] == ".java":
        with open("Code_to_AST/" + os.path.splitext(file_path)[0] + ".txt", "w") as f:
            command = 'java -cp ' + args.jar + ' JavaExtractor.App --max_path_length ' + \
                  str(args.max_path_length) + ' --max_path_width ' + str(args.max_path_width) + ' --file ' + args.file

            cmd = command.split()
            fd_popen = subprocess.Popen(cmd, stdout=subprocess.PIPE).stdout 
            ast = fd_popen.read().strip().decode("utf-8") 
            fd_popen.close()
            
            #name_path = os.path.splitext(file_path)[0]
            #data = os.path.splitext(file_path)[0] + " " + ast
            f.write(ast)

def get_immediate_subdirectories(a_dir):
    return [(os.path.join(a_dir, name)) for name in os.listdir(a_dir)
            if os.path.isdir(os.path.join(a_dir, name))]


TMP_DIR = ""

def ParallelExtractDir(args, dir):
    ExtractFeaturesForDir(args, dir, "")


def ExtractFeaturesForDir(args, dir, prefix):
    command = ['java', '-cp', args.jar, 'JavaExtractor.App',
               '--max_path_length', str(args.max_path_length), '--max_path_width', str(args.max_path_width),
               '--dir', dir, '--num_threads', str(args.num_threads)]

    
    #print(command)
    #os.system(command)
    kill = lambda process: process.kill()
    outputFileName = TMP_DIR + prefix + dir.split('/')[-1]
    failed = False
    with open(outputFileName, 'a') as outputFile:
        #print(outputFileName)
        sleeper = subprocess.Popen(command, stdout=outputFile, stderr=subprocess.PIPE)
        timer = Timer(600000, kill, [sleeper])

        try:
            timer.start()
            stdout, stderr = sleeper.communicate()
        finally:
            timer.cancel()

        if sleeper.poll() == 0:
            if len(stderr) > 0:
                print(sys.stderr, stderr, file=sys.stdout)
        else:
            print(sys.stderr, 'dir: ' + str(dir) + ' was not completed in time', file=sys.stdout)
            failed = True
            subdirs = get_immediate_subdirectories(dir)
            for subdir in subdirs:
                ExtractFeaturesForDir(args, subdir, prefix + dir.split('/')[-1] + '_')
    if failed:
        if os.path.exists(outputFileName):
            os.remove(outputFileName)


def ExtractFeaturesForDirsList(args, dirs):
    global TMP_DIR
    TMP_DIR = "./tmp/feature_extractor%d/" % (os.getpid())
    if os.path.exists(TMP_DIR):
        shutil.rmtree(TMP_DIR, ignore_errors=True)
    os.makedirs(TMP_DIR)
    try:
        p = multiprocessing.Pool(4)
        p.starmap(ParallelExtractDir, zip(itertools.repeat(args), dirs))
        #for dir in dirs:
        #    ExtractFeaturesForDir(args, dir, '')
        output_files = os.listdir(TMP_DIR)
        print(TMP_DIR)
        for f in output_files:
            #print("type %s" % (TMP_DIR, f))
            os.system("type %s" % (TMP_DIR, f))
            #os.system("cat %s/%s" % (TMP_DIR, f))
    finally:
        shutil.rmtree(TMP_DIR, ignore_errors=True)


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("-maxlen", "--max_path_length", dest="max_path_length", required=False, default=8)
    parser.add_argument("-maxwidth", "--max_path_width", dest="max_path_width", required=False, default=2)
    parser.add_argument("-threads", "--num_threads", dest="num_threads", required=False, default=64)
    parser.add_argument("-j", "--jar", dest="jar", required=True)
    parser.add_argument("-dir", "--dir", dest="dir", required=False)
    parser.add_argument("-file", "--file", dest="file", required=False)
    args = parser.parse_args()

    if args.file is not None:
        command = 'java -cp ' + args.jar + ' JavaExtractor.App --max_path_length ' + \
                  str(args.max_path_length) + ' --max_path_width ' + str(args.max_path_width) + ' --file ' + args.file
        os.system(command)
    elif args.dir is not None:
        temp_get_immediate_subdirectories(args, args.dir)
        
    '''
    elif args.dir is not None:
        subdirs = get_immediate_subdirectories(args.dir)
        to_extract = subdirs
        #print(subdirs)
        if len(subdirs) == 0:
            to_extract = [args.dir.rstrip('/')]
        ExtractFeaturesForDirsList(args, to_extract)
        #ExtractFeaturesForDirsList(args, args.dir)
    
    '''

