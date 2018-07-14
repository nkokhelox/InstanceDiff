# InstanceDiff
[![Build Status](https://travis-ci.org/nkokhelox/InstanceDiff.svg?branch=master)](https://travis-ci.org/nkokhelox/InstanceDiff)
[![Coverage Status](https://coveralls.io/repos/github/nkokhelox/InstanceDiff/badge.svg?branch=master)](https://coveralls.io/github/nkokhelox/InstanceDiff?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c084b72557984f59b489197300b2c0c1)](https://www.codacy.com/app/nkokhelox/InstanceDiff?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nkokhelox/InstanceDiff&amp;utm_campaign=Badge_Grade)

## Features
- Given 2 instances of the same java class then this program/library/utility can detected the difference in their properties using java reflections.
- Given inheritance hierarchy, then we can control how far up we can climb the inheritance hierarchy ladder when looking for differences.

  - ```InstanceDiff differentiator = new InstanceDiff(<INSTANCE OBJECT 1>, <INSTANCE OBJECT 2>)```
     
     *`<INSTANCE OBJECT 1>`, `<INSTANCE OBJECT 2>` are objects to be compared, all super classes considered when doing diff by default.*
  
  - ```InstanceDiff differentiator = new InstanceDiff(<INSTANCE OBJECT 1>, <INSTANCE OBJECT 2>, <HIERARCHY SCOPE LIMIT>)```
    
     *`<HIERARCHY SCOPE LIMIT>` is number of super classes to consider when looking for differences between `<INSTANCE OBJECT 1>` and `<INSTANCE OBJECT 2>`.*

- This can find the differences as follows: 
  - **All instance fields**
  
    ```Set<InstanceDiff.FieldDiff> diffSet = differentiator.getDiff();```

  - **Specific fields**
   
    ```Set<InstanceDiff.FieldDiff> diffSet = differentiator.getDiffOf(Set<String> fieldNamesToGetTheDiffOfOnly);```

  - **All fields excluding specific fields**
   
    ```Set<InstanceDiff.FieldDiff> diffSet = differentiator.getDiffExcluding(Set<String> fieldNamesToExcludeInDiff);```


 - InstanceDiff.FieldDiff = `Class{fieldName, valueOnInstance1, valueOnInstance2}`
 
 - Check the tests to further learn about this thing usage.
