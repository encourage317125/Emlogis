SELECT        t .[employeeId], c.RestrictionName, c.MaxValue, c.MinValue, c.RestrictionOrigin
FROM            T_Employee t CROSS apply
                             (SELECT        [employeeId], 'ConsecutiveDays', t .MaxConsecutiveDays, 0, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'HoursDay', t .MaxHoursDay, t .MinHoursDay, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'DailyOvertimeStart', t .BeginOvertimeDay, 0, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'WeeklyOvertimeStart', t .BeginOvertimeWeek, 0, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'TwoWeekOvertimeStart', t .BeginOvertimeTwoWeek, 0, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'HoursWeekPrimarySkill', 0, t .MinHoursWeekPrimarySkill, 'EmployeeContract'
                               UNION ALL
                               SELECT        NULL, 'HoursWeek', t .MaxHoursWeek, t .MinHoursWeek, 'EmployeeContract') c([employeeId], RestrictionName, MaxValue, MinValue, 
                         RestrictionOrigin)