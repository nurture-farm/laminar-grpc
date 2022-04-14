/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import React, { useState } from 'react';
import graphql from 'babel-plugin-relay/macro';
import { QueryRenderer } from 'react-relay';
import { DataType, SortDirection, SortingMode } from 'ka-table/enums';
import environment from './RelayEnv';
import { kaReducer, Table } from 'ka-table';
import { EditingMode } from 'ka-table/enums';
import "ka-table/style.scss";
import FilterControl from 'react-filter-control';
import { filterData } from './FilterData';

const listFarmersQuery = graphql`
  query ListFarmersQuery {
      executeListAllFarmers {
          status
          count
          records {
              farmerId
              farmerName
              epochs
              email
          }
      }
  }`

export const fields = [
  {
    caption: 'Farmer Name',
    name: 'farmerName',
    operators: [{
      caption: 'Contains',
      name: 'contains',
    }, {
      caption: 'Does not Contain',
      name: 'doesNotContain',
    }],
  },
  {
    caption: 'Email',
    name: 'email',
    operators: [{
      caption: 'Contains',
      name: 'contains',
    }, {
      caption: 'Does not Contain',
      name: 'doesNotContain',
    }],
  }, {
    caption: 'Create Time',
    name: 'epochs',
    operators: [{
      caption: 'Equals',
      name: '=',
    }, {
      caption: 'Does not Equal',
      name: '<>',
    }, {
      caption: 'More than',
      name: '>',
    }, {
      caption: 'Less than',
      name: '<',
    }],
  }];

export const groups = [{
  caption: 'And',
  name: 'and',
}, {
  caption: 'Or',
  name: 'or',
}];

export const defaultFilter = {
  groupName: 'and',
  items: [
    {
      field: 'farmerName',
      key: '1',
      operator: 'contains',
      value: '',
    }
  ],
};

const tablePropsInit = {
  columns: [
    { key: 'farmerId', title: 'Farmer Id', dataType: DataType.String },
    { key: 'farmerName', title: 'Farmer Name', dataType: DataType.String, sortDirection: SortDirection.Descend },
    { key: 'epochs', title: 'Create Time', dataType: DataType.String },
    { key: 'email', title: 'Email', dataType: DataType.String },
  ],
  editingMode: EditingMode.Cell,
  paging: {
    enabled: true,
    pageIndex: 0,
    pageSize: 10
  },
  rowKeyField: 'farmerId',
  sortingMode: SortingMode.Single,
};

const ListFramers = () => {
  //table
  const [tableProps, changeTableProps] = useState(tablePropsInit);
  const dispatch = (action) => {
    changeTableProps((prevState) => kaReducer(prevState, action));
  };

  //filter
  const [filterValue, changeFilter] = useState(defaultFilter);
  const onFilterChanged = (newFilterValue) => {
    changeFilter(newFilterValue);
  };

  return (
    <div className="h-screen bg-gray-200 pt-4">
      <QueryRenderer
        environment={environment}
        query={listFarmersQuery}
        variables={{}}
        render={({ error, props }) => {
          if (error) {
            return <div>Error!</div>;
          } else if (props) {
            return (
              <div className='container mx-auto'>
                <label className="text-3xl font-sans font-medium block">Farmer List</label>
                <FilterControl {...{ fields, groups, filterValue, onFilterValueChanged: onFilterChanged }} />
                <div className="rounded overflow-hidden shadow-lg">
                  <Table
                    {...tableProps}
                    data={props.executeListAllFarmers.records}
                    dispatch={dispatch}
                    extendedFilter={(data) => filterData(data, filterValue)}
                  />
                </div>
              </div>
            );
          }
        }}
      />
    </div>
  );
}

export default ListFramers;