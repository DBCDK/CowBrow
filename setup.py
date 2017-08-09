from setuptools import setup, find_packages
# To use a consistent encoding
from codecs import open
from os import path

here = path.abspath(path.dirname(__file__))

# Get the long description from the README file
with open(path.join(here, 'README.md'), encoding='utf-8') as f:
    long_description = f.read()

setup(
    name='CowBrow_python_REST_client',

    # Versions should comply with PEP440.  For a discussion on single-sourcing
    # the version across setup.py and the project code, see
    # https://packaging.python.org/en/latest/single_source_version.html
    version='0.1',

    description='Python classes to seamlessly get info from MQ brokers, through CowBrow rest middle layer.',
    long_description=long_description,

    url='https://github.com/DBCDK/CowBrow',

    author='dataio',
    author_email='dataio@dbc.dk',

    license='GPLv3',

    classifiers=[

        'Development Status :: 3 - Alpha',

        'Intended Audience :: Developers',
        'Topic :: Software Development :: MQ Tools',

        'License :: OSI Approved :: GPL License',


        'Programming Language :: Python :: 3.5',
    ],

    keywords='MQ python3 broker',
    packages=['ui', 'ui.mq_python'],

    install_requires=['requests'],
    entry_points={
        'console_scripts': [

        ],
    },
)