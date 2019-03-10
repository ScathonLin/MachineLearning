import configparser
import os

from utils.logger import *


class CommonUtils:
    @staticmethod
    def create_config_parser(config_file_name) -> configparser.ConfigParser:
        if not os.path.exists(config_file_name):
            logger.error('config file is not exists: %s' % config_file_name)
            return None
        cf = configparser.ConfigParser()
        cf.read(config_file_name)
        return cf

    @staticmethod
    def load_config_to_dict(config_file_path) -> dict:
        cf = CommonUtils.create_config_parser(config_file_path)
        secs = cf.sections()
        conf_list = []
        for sec in secs:
            conf_list.extend(list(map(lambda item: item, cf.items(sec))))
        #  这句代码和上面两句代码效果是一样的.
        # list(map(lambda sec: conf_list.extend(list(map(lambda item: item, cf.items(sec)))), cf.sections()))
        conf_dict = dict()
        for conf_tuple in conf_list:
            conf_dict[conf_tuple[0]] = conf_tuple[1]
        return conf_dict
