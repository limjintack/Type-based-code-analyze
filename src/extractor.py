import subprocess
import os

from common import PathContextInformation

class Extractor:
    def __init__(self, config, jar_path, max_path_length, max_path_width):
        self.config = config
        self.max_path_length = max_path_length
        self.max_path_width = max_path_width
        self.jar_path = jar_path

    def extract_paths(self, path):
        
        dirpath = os.getcwd()
        filename = os.path.join(dirpath,path)
        
        jar_p = os.path.join(dirpath,self.jar_path)
        
        
        command = ['java', '-cp', jar_p, 'JavaExtractor.App', '--max_path_length',
                   str(self.max_path_length), '--max_path_width', str(self.max_path_width), '--file', filename]
        
        
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()
        output = out.decode().splitlines()
        
        if len(output) == 0:
            err = err.decode()
            raise ValueError(err)        
        pc_info_dict = {}
        result = []
        for i, line in enumerate(output):
            parts = line.rstrip().split(' ')
            method_name = parts[0]
            current_result_line_parts = [method_name]
            contexts = parts[1:]
            for context in contexts[:self.config.MAX_CONTEXTS]:
                pc_info = PathContextInformation(context)
                current_result_line_parts += [str(pc_info)]
                pc_info_dict[(pc_info.token1, pc_info.shortPath, pc_info.token2)] = pc_info
            space_padding = ' ' * (self.config.DATA_NUM_CONTEXTS - len(contexts))
            result_line = ' '.join(current_result_line_parts) + space_padding
            result.append(result_line)
        return result, pc_info_dict

    @staticmethod
    def java_string_hashcode(s):
        """
        Imitating Java's String#hashCode, because the model is trained on hashed paths but we wish to
        Present the path attention on un-hashed paths.
        """
        h = 0
        for c in s:
            h = (31 * h + ord(c)) & 0xFFFFFFFF
        return ((h + 0x80000000) & 0xFFFFFFFF) - 0x80000000
