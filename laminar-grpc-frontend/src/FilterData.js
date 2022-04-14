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

export const isEmpty = (value) => (value == null || value.length === 0);

const contains = (data, item) => {
    if (!item.value) { return true; }
    return data[item.field].toLowerCase().includes(item.value.toLowerCase());
};
const doesNotContain = (data, item) => {
    if (!item.value) { return true; }
    return !data[item.field].toLowerCase().includes(item.value.toLowerCase());
};
const equals = (data, item) => {
    if (!item.value) { return true; }
    return data[item.field].toString().toLowerCase() === item.value.toString().toLowerCase();
};
const isNotEqual = (data, item) => {
    if (!item.value) { return true; }
    return data[item.field].toString().toLowerCase() !== item.value.toString().toLowerCase();
};
const more = (data, item) => data[item.field] > item.value;
const less = (data, item) => data[item.field] < item.value;
export const filterItem = (data, filter) => {
    switch (filter.operator) {
        case 'contains': return contains(data, filter);
        case 'doesNotContain': return doesNotContain(data, filter);
        case '=': return equals(data, filter);
        case '<>': return isNotEqual(data, filter);
        case '>': return more(data, filter);
        case '<': return less(data, filter);
        default: throw Error('unknown operator');
    }
};

export const filterGroup = (data, groupName, items) =>
    (groupName.toLowerCase() === 'or' ? filterGroupOr(data, items) : filterGroupAnd(data, items));

export const filterGroupOr = (data, items) => {
    const filteredData = items.reduce((initialData, item) => {
        if (item.items) {
            const grouped = filterGroup(data, item.groupName, item.items);
            return initialData.concat(grouped.filter((d) => initialData.indexOf(d) < 0));
        }
        return initialData.concat(data.filter((d) => initialData.indexOf(d) < 0 && filterItem(d, item)));
    }, []);
    return data.filter((d) => filteredData.includes(d));
};

export const filterGroupAnd = (data, items) => {
    return items.reduce((initialData, item) => {
        if (item.items) { return filterGroup(initialData, item.groupName, item.items); }
        return initialData.filter((d) => filterItem(d, item));
    }, data);
};

export const filterData = (data, filterValue) => {
    return filterGroup(data, filterValue.groupName, filterValue.items);
};
